package mindustry.core;

import mindustry.world.Block;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
//Outdated!
public class TypingsGenerator {
    private static void generate(Class entryClass, HashMap<Class, TypingsClass> generated){
        final TypingsClass typingsClass = new TypingsClass();
        typingsClass.namespace = escapeNamespaces(entryClass.getCanonicalName());
        generated.put(entryClass, typingsClass);
        StringBuilder sb = new StringBuilder("export interface ");
        String tsName = entryClass.getSimpleName();
        sb.append(tsName);

        sb.append(" {\n");

        HashMap<String, ArrayList<String>> properties = new HashMap<>();
        HashMap<String, ArrayList<String>> staticProperties = new HashMap<>();

        Class entrySuper = entryClass;

        while(entrySuper != null){
            for(Field field: entrySuper.getDeclaredFields()){
                final int fieldModifiers = field.getModifiers();
                if(!Modifier.isPublic(fieldModifiers)) continue;
                StringBuilder property = new StringBuilder("    ");
                Class fieldType = field.getType();
                while(fieldType.isArray()) fieldType = fieldType.getComponentType();
                if(fieldType.isPrimitive()) fieldType = toBoxedType(fieldType);
                if(!generated.containsKey(fieldType)){
                    generate(fieldType, generated);
                }
                if(Modifier.isStatic(fieldModifiers)){
                    property.append("static ");
                }
                final String name = field.getName();
                property.append(name);
                property.append(": ");
                property.append(toTSName(fieldType));
                property.append(";\n");
                final HashMap<String, ArrayList<String>> correctProperties =
                        Modifier.isStatic(fieldModifiers) ? staticProperties : properties;
                if(!correctProperties.containsKey(name)){
                    correctProperties.put(name, new ArrayList<>());
                }
                correctProperties.get(name).add(property.toString());
            }

            for(Method method: entrySuper.getDeclaredMethods()){
                final int methodModifiers = method.getModifiers();
                if(!Modifier.isPublic(methodModifiers)) continue;
                StringBuilder property = new StringBuilder("    ");
                Class returnType = method.getReturnType();
                while(returnType.isArray()) returnType = returnType.getComponentType();
                if(returnType.isPrimitive()) returnType = toBoxedType(returnType);
                if(!generated.containsKey(returnType)){
                    generate(returnType, generated);
                }
                if(Modifier.isStatic(methodModifiers)){
                    property.append("static ");
                }
                final String name = method.getName();
                property.append(name);
                property.append(": (");
                final Parameter[] params = method.getParameters();
                for(Parameter param: params){
                    Class paramType = param.getType();
                    while(paramType.isArray()) paramType = paramType.getComponentType();
                    if(paramType.isPrimitive()) paramType = toBoxedType(paramType);
                    if(!generated.containsKey(paramType)){
                        generate(paramType, generated);
                    }
                    property.append(param.getName());
                    if(isKeyWord(param.getName())) property.append('_');
                    property.append(": ");
                    property.append(toTSName(paramType));
                    if(param != params[params.length - 1]){
                        property.append(", ");
                    }
                }
                property.append(") => ");
                property.append(toTSName(returnType));
                property.append(";\n");
                final HashMap<String, ArrayList<String>> correctProperties =
                        Modifier.isStatic(methodModifiers) ? staticProperties : properties;
                if(!correctProperties.containsKey(name)){
                    correctProperties.put(name, new ArrayList<>());
                }
                correctProperties.get(name).add(property.toString());
            }
            entrySuper = entrySuper.getSuperclass();
        }

        for(HashMap.Entry<String, ArrayList<String>> property: properties.entrySet()){
            sb.append(mergeProperties(property.getValue()));
        }
        sb.append("}\nexport class ");
        sb.append(tsName);
        sb.append(" {");
        for(HashMap.Entry<String, ArrayList<String>> property: staticProperties.entrySet()){
            sb.append(mergeProperties(property.getValue()));
        }
        sb.append("}\n");
        typingsClass.code = sb.toString();
    }
    private static String mergeProperties(ArrayList<String> properties){
        return properties.get(0);
    }
    private static String toTSName(Class cl){
        StringBuilder arrayAmount = new StringBuilder();
        Class type = cl;
        while(type.isArray()){
            arrayAmount.append("[]");
            type = type.getComponentType();
        }
        String arr = arrayAmount.toString();
        String specialCase = specialCaseTSType(type);
        if(specialCase.length() > 0){
            specialCase = " | " + specialCase + arr;
        }
        return "Packages." + escapeNamespaces(toBoxedType(type).getName()) + arr + specialCase;
    }
    private static String specialCaseTSType(Class cl){
        if(cl.equals(void.class) || cl.equals(Void.class)) return "void";
        if(cl.equals(float.class) || cl.equals(Float.class)) return "number";
        if(cl.equals(double.class) || cl.equals(Double.class)) return "number";
        if(cl.equals(boolean.class) || cl.equals(Boolean.class)) return "boolean";
        if(cl.equals(byte.class) || cl.equals(Byte.class)) return "number";
        if(cl.equals(char.class) || cl.equals(Character.class)) return "string";
        if(cl.equals(short.class) || cl.equals(Short.class)) return "number";
        if(cl.equals(int.class) || cl.equals(Integer.class)) return "number";
        if(cl.equals(long.class) || cl.equals(Long.class)) return "number";
        if(cl.equals(String.class)) return "string";
        return "";
    }
    private static Class toBoxedType(Class cl){
        if(cl.equals(void.class)) return Void.class;
        if(cl.equals(float.class)) return Float.class;
        if(cl.equals(double.class)) return Double.class;
        if(cl.equals(boolean.class)) return Boolean.class;
        if(cl.equals(byte.class)) return Number.class;
        if(cl.equals(char.class)) return String.class;
        if(cl.equals(short.class)) return Short.class;
        if(cl.equals(int.class)) return Integer.class;
        if(cl.equals(long.class)) return Long.class;
        return cl;
    }
    private static String escapeNamespaces(String ns){
        String[] nss = ns.split("\\.");
        for(int i = 0; i < nss.length; i++){
            if(isKeyWord(nss[i])) nss[i] += '_';
        }
        return String.join(".", nss).replaceAll("\\$", ".");
    }
    private static boolean isKeyWord(String ns){
        switch (ns){
            case "function":
            case "if":
            case "while":
            case "class":
            case "namespace":
            case "for":
            case "constructor":
            case "in":
                return true;
            default: return false;
        }
    }
    public static String generate(Class ... entries){
        final HashMap<Class, TypingsClass> generated = new HashMap<>();
        Namespace namespace = new Namespace("Packages");
        for(Class entry: entries) generate(entry, generated);
        for (TypingsClass typingsClass : generated.values()) {
            Namespace current = namespace;
            String[] names = typingsClass.namespace.split("\\.");
            for(String name: names){
                if(name.equals(names[names.length - 1])) break;
                current = current.getOrCreateNameSpace(name);
            }
            current.sb.append(typingsClass.code);
        }
        return namespace.toString();
    }
    static class TypingsClass{
        public String namespace;
        public String code;
    }
    static class Namespace{
        StringBuilder sb;
        HashMap<String, Namespace> children;
        public Namespace(String name){
            sb = new StringBuilder("export namespace ").append(name).append(" {\n");
            children = new HashMap<>();
        }
        public Namespace getOrCreateNameSpace(String name){
            Namespace child = children.get(name);
            if(child != null){
                return child;
            }
            child = new Namespace(name);
            children.put(name, child);
            return child;
        }

        @Override
        public String toString() {
            for(Namespace ns: children.values()) sb.append(ns.toString());
            return sb.append("}\n").toString();
        }
    }
}
