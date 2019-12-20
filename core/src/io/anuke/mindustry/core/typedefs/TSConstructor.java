package io.anuke.mindustry.core.typedefs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

public class TSConstructor implements TSConvertable {
    private Constructor base;
    public TSConstructor(Constructor constructor){
        base = constructor;
    }
    @Override
    public String toString(TypeConverter tc) {
        StringBuilder sb = new StringBuilder("constructor(");
        final Parameter[] params = base.getParameters();
        for(Parameter param: params){
            final Class paramType = param.getType();
            try {
                tc.resolveClass(paramType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.append(tc.escapeKeywords(param.getName()));
            sb.append(": ");
            sb.append(tc.toTSType(paramType));
            if(param != params[params.length - 1]) sb.append(", ");
        }
        sb.append(')');
        return sb.toString();
    }
}
