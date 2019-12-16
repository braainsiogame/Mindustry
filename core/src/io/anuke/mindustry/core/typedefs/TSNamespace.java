package io.anuke.mindustry.core.typedefs;

import java.util.ArrayList;
import java.util.HashMap;

public class TSNamespace implements TSConvertable{
    public StringBuilder content;
    public HashMap<String, TSNamespace> children;
    public String name;
    public TSNamespace(String name){
        content = new StringBuilder("export namespace ");
        content.append(name);
        content.append(" {\n");
        children = new HashMap<>();
        this.name = name;
    }
    public TSNamespace child(String name){
        TSNamespace child = children.get(name);
        if(child == null){
            child = new TSNamespace(name);
            children.put(name, child);
        }
        return child;
    }
    @Override
    public String toString(TypeConverter tc) {
        for(TSNamespace child: children.values()){
            content.append(child.toString(tc));
        }
        content.append("}\n");
        return content.toString();
    }
}
