package io.anuke.mindustry.core.typedefs;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TSClass implements TSConvertable {
    public Class base;
    public HashMap<String, TSField> staticTSFields;
    public HashMap<String, TSField> tsFields;
    public HashMap<String, ArrayList<TSMethod>> staticTSMethods;
    public HashMap<String, ArrayList<TSMethod>> tsMethods;
    public ArrayList<TSConstructor> constructors;
    public TSClass(Class base){
        this.base = base;
        staticTSFields = new HashMap<>();
        tsFields = new HashMap<>();
        staticTSMethods = new HashMap<>();
        tsMethods = new HashMap<>();
        constructors = new ArrayList<>();
    }

    @Override
    public String toString(TypeConverter tc) {
        StringBuilder sb = new StringBuilder();

        for(Field field: base.getFields()){
            final int modifiers = field.getModifiers();
            if(!Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers)){
                final String fieldName = field.getName();
                final HashMap<String, TSField> properFields =
                        Modifier.isStatic(modifiers) ? staticTSFields : tsFields;
                properFields.putIfAbsent(fieldName, new TSField(field));
            }
        }
        for(Method method: base.getMethods()){
            final int modifiers = method.getModifiers();
            if(!Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers)){
                final String methodName = method.getName();
                final HashMap<String, ArrayList<TSMethod>> properMethods =
                        Modifier.isStatic(modifiers) ? staticTSMethods : tsMethods;
                ArrayList<TSMethod> methods = properMethods.computeIfAbsent(methodName, k -> new ArrayList<>());
                methods.add(new TSMethod(method));
            }
        }
        for(Constructor constructor: base.getConstructors()){
            final int modifiers = constructor.getModifiers();
            if(Modifier.isPublic(modifiers)){
                constructors.add(new TSConstructor(constructor));
            }
        }

        sb.append("export class ");
        sb.append(base.getSimpleName());
        sb.append(" {\n");
        handleFields(staticTSFields, tc, sb);
        handleFields(tsFields, tc, sb);
        handleMethods(staticTSMethods, tc, sb);
        handleMethods(tsMethods, tc, sb);
        if(constructors.size() > 0){
            for(TSConstructor constructor: constructors){
                sb.append(constructor.toString(tc));
                sb.append(";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
    private void handleMethods(HashMap<String, ArrayList<TSMethod>> methods, TypeConverter tc, StringBuilder sb){
        for(HashMap.Entry<String, ArrayList<TSMethod>> entry: methods.entrySet()){
            final String name = entry.getKey();
            for(TSMethod method: entry.getValue()){
                if(methods == staticTSMethods){
                    sb.append("static ");
                }
                sb.append(name);
                if(methods == tsMethods && name.equals("constructor")){
                    sb.append('_');
                }
                sb.append(method.toString(tc));
                sb.append(";\n");
            }
        }
    }
    private void handleFields(HashMap<String, TSField> fields, TypeConverter tc, StringBuilder sb){
        for(HashMap.Entry<String, TSField> entry: fields.entrySet()){
            if((fields == staticTSFields ? staticTSMethods : tsMethods).containsKey(entry.getKey())){
                continue;
            }
            if(fields == staticTSFields){
                sb.append("static ");
            }
            final String name = entry.getKey();
            sb.append(name);
            if(fields == tsFields && name.equals("constructor")){
                sb.append('_');
            }
            sb.append(": ");
            sb.append(entry.getValue().toString(tc));
            sb.append(";\n");
        }
    }
}
