package io.anuke.mindustry.core.typedefs;

import io.anuke.arc.util.Log;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TSClass implements TSConvertable {
    public Class base;
    public HashMap<String, ArrayList<TSConvertable>> staticProperties;
    public HashMap<String, ArrayList<TSConvertable>> properties;
    public TSClass(Class base){
        this.base = base;
        staticProperties = new HashMap<>();
        properties = new HashMap<>();
    }

    @Override
    public String toString(TypeConverter tc) {
        StringBuilder sb = new StringBuilder();

        Class baseSuper = base;
        while(baseSuper != null){
            for(Field field: baseSuper.getDeclaredFields()){
                final int modifiers = field.getModifiers();
                if(Modifier.isPublic(modifiers)){
                    final String fieldName = field.getName();
                    final HashMap<String, ArrayList<TSConvertable>> properProperties =
                            Modifier.isStatic(modifiers) ? staticProperties : properties;
                    ArrayList<TSConvertable> fields = properProperties.computeIfAbsent(fieldName, k -> new ArrayList<>());
                    fields.add(new TSField(field));
                }
            }
            for(Method method: baseSuper.getDeclaredMethods()){
                final int modifiers = method.getModifiers();
                if(Modifier.isPublic(modifiers)){
                    final String methodName = method.getName();
                    final HashMap<String, ArrayList<TSConvertable>> properProperties =
                            Modifier.isStatic(modifiers) ? staticProperties : properties;
                    ArrayList<TSConvertable> methods = properProperties.computeIfAbsent(methodName, k -> new ArrayList<>());
                    methods.add(new TSMethod(method));
                }
            }
            baseSuper = baseSuper.getSuperclass();
        }

        sb.append("export class ");
        handleProperties(staticProperties, tc, sb);

        sb.append("export interface ");
        handleProperties(properties, tc, sb);

        return sb.toString();
    }
    private void handleProperties(HashMap<String, ArrayList<TSConvertable>> properties, TypeConverter tc, StringBuilder sb){
        final String className = base.getSimpleName();
        sb.append(className);
        sb.append(" {\n");

        for(HashMap.Entry<String, ArrayList<TSConvertable>> entry: properties.entrySet()){
            sb.append("    ");
            if(properties == staticProperties){
                sb.append("static ");
            }
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(tc.mergeProperties(entry.getValue()));
            sb.append(";\n");
        }

        sb.append("}\n");
    }
}
