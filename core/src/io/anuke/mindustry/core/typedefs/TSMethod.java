package io.anuke.mindustry.core.typedefs;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class TSMethod implements TSConvertable {
    private Method base;
    public TSMethod(Method method){
        base = method;
    }
    @Override
    public String toString(TypeConverter tc) {
        StringBuilder sb = new StringBuilder("(");
        final Parameter[] params = base.getParameters();
        for(Parameter param: params){
            final Class paramType = param.getType();
            tc.resolveClass(paramType);
            sb.append(tc.escapeKeywords(param.getName()));
            sb.append(": ");
            sb.append(tc.toTSType(paramType));
            if(param != params[params.length - 1]) sb.append(", ");
        }
        sb.append("): ");
        final Class returnType = base.getReturnType();
        tc.resolveClass(returnType);
        sb.append(tc.toTSType(returnType));
        return sb.toString();
    }
}
