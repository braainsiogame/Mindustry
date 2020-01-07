package mindustry.core.typedefs;

import java.io.IOException;
import java.lang.reflect.Field;

public class TSField implements TSConvertable {
    public Class type;
    public TSField(Field field){
        type = field.getType();
    }
    @Override
    public String toString(TypeConverter tc) {
        try {
            tc.resolveClass(type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tc.toTSType(type);
    }
}
