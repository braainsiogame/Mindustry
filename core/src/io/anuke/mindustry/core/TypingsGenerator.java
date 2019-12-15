package io.anuke.mindustry.core;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class TypingsGenerator {
    private static void generate(Class entry, HashMap<Class, TypingsClass> generated){
        if(entry.isArray()) return;
        if(entry.equals(Void.class)) return;
        if(entry.equals(float.class) || entry.equals(Float.class)) return;
        if(entry.equals(double.class) || entry.equals(Double.class)) return;
        if(entry.equals(boolean.class) || entry.equals(Boolean.class)) return;
        if(entry.equals(byte.class) || entry.equals(Byte.class)) return;
        if(entry.equals(char.class) || entry.equals(Character.class)) return;
        if(entry.equals(short.class) || entry.equals(Short.class)) return;
        if(entry.equals(int.class) || entry.equals(Integer.class)) return;
        if(entry.equals(long.class) || entry.equals(Long.class)) return;
        if(entry.equals(String.class)) return;

        final TypingsClass typingsClass = new TypingsClass();
        typingsClass.namespace = escapeNamespaces(entry.getCanonicalName());
        generated.put(entry, typingsClass);
        StringBuilder sb = new StringBuilder("export class ");
        sb.append(entry.getSimpleName());
        sb.append(" {\n");
        HashMap<String, String> properties = new HashMap<>();
        HashMap<String, String> staticProperties = new HashMap<>();
        for(Field field: entry.getDeclaredFields()){
            final int fieldModifiers = field.getModifiers();
            if(!Modifier.isPublic(fieldModifiers)) continue;
            StringBuilder property = new StringBuilder("    ");
            final Class fieldType = field.getType();
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
            (Modifier.isStatic(fieldModifiers) ? staticProperties : properties).put(name, property.toString());
        }
        for(Method method: entry.getDeclaredMethods()){
            final int methodModifiers = method.getModifiers();
            if(!Modifier.isPublic(methodModifiers)) continue;
            StringBuilder property = new StringBuilder("    ");
            final Class returnType = method.getReturnType();
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
                final Class paramType = param.getType();
                if(!generated.containsKey(paramType)){
                    generate(paramType, generated);
                }
                property.append(param.getName());
                property.append(": ");
                property.append(toTSName(paramType));
                if(param != params[params.length - 1]){
                    property.append(", ");
                }
            }
            property.append(") => ");
            property.append(toTSName(returnType));
            property.append(";\n");
            final HashMap<String, String> correct_properties = Modifier.isStatic(methodModifiers) ? staticProperties : properties;
            correct_properties.put(name, correct_properties.containsKey(name) ? "" : property.toString());
        }
        for(HashMap.Entry property: staticProperties.entrySet()){
            if(property.getValue().equals("")){
                sb.append("    static ");
                String key = (String) property.getKey();
                sb.append(key);
                if(isKeyWord(key)) sb.append('_');
                sb.append(": any;\n");
            } else {
                sb.append(property.getValue());
            }
        }
        for(HashMap.Entry property: properties.entrySet()){
            if(property.getValue().equals("")){
                sb.append("    ");
                String key = (String) property.getKey();
                sb.append(key);
                if(key.startsWith("cons")) System.out.println(key);
                if(isKeyWord(key)) sb.append('_');
                sb.append(": any;\n");
            } else {
                sb.append(property.getValue());
            }
        }
        sb.append("}\n");
        typingsClass.code = sb.toString();
    }
    private static String toTSName(Class cl){
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

        if(cl.equals(float[].class) || cl.equals(Float[].class)) return "number[]";
        if(cl.equals(double[].class) || cl.equals(Double[].class)) return "number[]";
        if(cl.equals(boolean[].class) || cl.equals(Boolean[].class)) return "boolean";
        if(cl.equals(byte[].class) || cl.equals(Byte[].class)) return "number[]";
        if(cl.equals(char[].class) || cl.equals(Character[].class)) return "string[]";
        if(cl.equals(short[].class) || cl.equals(Short[].class)) return "number[]";
        if(cl.equals(int[].class) || cl.equals(Integer[].class)) return "number[]";
        if(cl.equals(long[].class) || cl.equals(Long[].class)) return "number[]";
        if(cl.equals(String[].class)) return "string[]";

        return escapeNamespaces(cl.getCanonicalName());
    }
    private static String escapeNamespaces(String ns){
        String[] nss = ns.split("\\.");
        for(int i = 0; i < nss.length; i++){
            if(isKeyWord(nss[i])) nss[i] += '_';
        }
        return String.join(".", nss);
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
                return true;
            default: return false;
        }
    }
    public static String generate(Class entry){
        final HashMap<Class, TypingsClass> generated = new HashMap<>();
        Namespace namespace = new Namespace("typings");
        generate(entry, generated);
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
