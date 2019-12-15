package io.anuke.mindustry.core;

import java.lang.reflect.*;
import java.util.HashMap;

public class TypingsGenerator {
    private static void generate(Class entry, HashMap<Class, String> generated){
        if(entry.isArray()) return;
        if(entry.equals(Void.class)) return;
        if(entry.equals(float.class)) return;
        if(entry.equals(double.class)) return;
        if(entry.equals(boolean.class)) return;
        if(entry.equals(byte.class)) return;
        if(entry.equals(short.class)) return;
        if(entry.equals(int.class)) return;
        if(entry.equals(long.class)) return;
        if(entry.equals(String.class)) return;

        generated.put(entry, "");
        StringBuilder sb = new StringBuilder("export declare class ");
        sb.append(toTSName(entry));
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
                sb.append(property.getKey());
                sb.append(": any;\n");
            } else {
                sb.append(property.getValue());
            }
        }
        for(HashMap.Entry property: properties.entrySet()){
            if(property.getValue().equals("")){
                sb.append("    ");
                sb.append(property.getKey());
                sb.append(": any;\n");
            } else {
                sb.append(property.getValue());
            }
        }
        sb.append("}\n");
        generated.put(entry, sb.toString());
    }
    private static String toTSName(Class cl){
        if(cl.equals(Void.class)) return "void";
        if(cl.equals(float.class)) return "number";
        if(cl.equals(double.class)) return "number";
        if(cl.equals(boolean.class)) return "boolean";
        if(cl.equals(byte.class)) return "number";
        if(cl.equals(short.class)) return "number";
        if(cl.equals(int.class)) return "number";
        if(cl.equals(long.class)) return "number";
        if(cl.equals(String.class)) return "string";

        if(cl.equals(float[].class)) return "number[]";
        if(cl.equals(double[].class)) return "number[]";
        if(cl.equals(boolean[].class)) return "boolean";
        if(cl.equals(byte[].class)) return "number[]";
        if(cl.equals(short[].class)) return "number[]";
        if(cl.equals(int[].class)) return "number[]";
        if(cl.equals(long[].class)) return "number[]";
        if(cl.equals(String[].class)) return "string[]";

        String name = cl.getCanonicalName();
        StringBuilder sb = new StringBuilder();
        for(int i = 0, n = name.length() ; i < n ; i++) {
            char c = name.charAt(i);
            if(c == '.'){
                sb.append('_');
                continue;
            }
            if(c == '_'){
                sb.append('_');
            }
            sb.append(c);
        }
        return sb.toString();
    }
    public static String generate(Class entry){
        final HashMap<Class, String> generated = new HashMap<>();
        StringBuilder result = new StringBuilder();
        generate(entry, generated);
        for (String string : generated.values()) {
            result.append(string);
        }
        return result.toString();
    }
}
