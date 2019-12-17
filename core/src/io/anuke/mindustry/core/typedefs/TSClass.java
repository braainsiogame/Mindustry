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
    public ArrayList<TSConstructor> constructors;
    public TSClass(Class base){
        this.base = base;
        staticProperties = new HashMap<>();
        properties = new HashMap<>();
        constructors = new ArrayList<>();
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
            for(Constructor constructor: baseSuper.getDeclaredConstructors()){
                final int modifiers = constructor.getModifiers();
                if(Modifier.isPublic(modifiers)){
                    constructors.add(new TSConstructor(constructor));
                }
            }
            baseSuper = baseSuper.getSuperclass();
        }

        sb.append("export class ");
        sb.append(base.getSimpleName());
        sb.append(" {\n");
        handleProperties(staticProperties, tc, sb);
        handleProperties(properties, tc, sb);
        if(constructors.size() > 0){
            sb.append(tc.mergeConstructors(constructors));
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
    private void handleProperties(HashMap<String, ArrayList<TSConvertable>> properties, TypeConverter tc, StringBuilder sb){
        for(HashMap.Entry<String, ArrayList<TSConvertable>> entry: properties.entrySet()){
            sb.append("    ");
            if(properties == staticProperties){
                sb.append("static ");
            }
            final String key = entry.getKey();
            sb.append(key);
            if(properties != staticProperties && key.equals("constructor")){
                sb.append('_');
            }
            sb.append(": ");
            sb.append(tc.mergeProperties(entry.getValue()));
            sb.append(";\n");
        }
    }
}
