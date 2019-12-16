package io.anuke.mindustry.core.typedefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TypeConverter {
    private static HashMap<Class, Class> toBoxed = new HashMap<>();
    private static HashMap<Class, String> toTSName = new HashMap<>();
    static {
        toBoxed.put(void.class, Void.class);
        toBoxed.put(float.class, Float.class);
        toBoxed.put(double.class, Double.class);
        toBoxed.put(boolean.class, Boolean.class);
        toBoxed.put(byte.class, Byte.class);
        toBoxed.put(char.class, Character.class);
        toBoxed.put(short.class, Short.class);
        toBoxed.put(int.class, Integer.class);
        toBoxed.put(long.class, Long.class);

        toTSName.put(Void.class, "void");
        toTSName.put(Float.class, "number");
        toTSName.put(Double.class, "number");
        toTSName.put(Boolean.class, "boolean");
        toTSName.put(Byte.class, "number");
        toTSName.put(Character.class, "string");
        toTSName.put(Short.class, "number");
        toTSName.put(Integer.class, "number");
        toTSName.put(Long.class, "number");
        toTSName.put(String.class, "string");
    }
    private TSNamespace namespace;
    private HashSet<Class> resolved;
    public TypeConverter(){
        namespace = new TSNamespace("Packages");
        resolved = new HashSet<>();
    }
    private String escapeNamespaces(String ns){
        String[] nss = ns.split("\\.");
        for(int i = 0; i < nss.length; i++){
            nss[i] = escapeKeywords(nss[i]);
        }
        return String.join(".", nss).replaceAll("\\$", ".");
    }
    public void resolveClass(Class type){
        Class base = type;
        while(base.isArray()){
            base = base.getComponentType();
        }
        if(base.isPrimitive()) base = toBoxed.get(base);
        if(resolved.contains(base)) return;
        //System.out.println(base);
        resolved.add(base);
        TSClass tsClass = new TSClass(base);
        String[] names = escapeNamespaces(base.getCanonicalName()).split("\\.");
        TSNamespace currentNS = namespace;
        for(String name: names){
            if(name.equals(names[names.length - 1])){
                currentNS.content.append(tsClass.toString(this));
            } else {
                currentNS = currentNS.child(name);
            }
        }
    }
    public String mergeProperties(ArrayList<TSConvertable> properties){
        if(properties.size() > 1) return "any";
        return properties.get(0).toString(this);
    }
    public String toTSType(Class type){
        StringBuilder arrayAmountBuilder = new StringBuilder();
        Class baseType = type;
        while(baseType.isArray()){
            arrayAmountBuilder.append("[]");
            baseType = baseType.getComponentType();
        }
        String arrayAmount = arrayAmountBuilder.toString();
        StringBuilder sb = new StringBuilder("Packages.");
        Class boxed = toBoxed.getOrDefault(baseType, baseType);
        sb.append(escapeNamespaces(boxed.getName()));
        sb.append(arrayAmount);
        String tsName = toTSName.get(boxed);
        if(tsName != null){
            sb.append(" | ");
            sb.append(tsName);
            sb.append(arrayAmount);
        }
        return sb.toString();
    }
    public String escapeKeywords(String string){
        switch (string){
            case "else":
            case "while":
            case "for":
            case "if":
            case "in":
            case "of":
            case "await":
            case "async":
            case "function":
                string += '_';
            default:
                return string;
        }
    }

    @Override
    public String toString() {
        return namespace.toString(this);
    }
}
