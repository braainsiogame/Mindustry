package io.anuke.mindustry.core.typedefs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TSModule {
    public HashMap<String, TSModule> children;
    private FileOutputStream stream;
    public Path path;
    public Path root;
    public TSModule(Path path, Path root) throws IOException {
        path.toFile().mkdir();
        stream = new FileOutputStream(path.resolve("index.d.ts").toFile());
        children = new HashMap<>();
        this.path = path;
        this.root = root;
        if(!path.equals(root)){
            stream.write("import * as Packages from \"".getBytes());
            Path current = path;
            while(!current.equals(root)){
                stream.write("../".getBytes());
                current = current.getParent();
            }
            stream.write("\";\n".getBytes());
        }
    }
    public TSModule(Path path) throws IOException {
        this(path, path);
    }
    public void add(String string) throws IOException {
        stream.write(string.getBytes());
    }
    public TSModule child(String name) throws IOException {
        TSModule child = children.get(name);
        if(child == null){
            child = new TSModule(path.resolve(name), this.root);
            children.put(name, child);
        }
        return child;
    }
    public void finish() throws IOException {
        Set<HashMap.Entry<String, TSModule>> entries = children.entrySet();
        String[] childrenNames = new String[entries.size()];
        int i = 0;
        for(HashMap.Entry<String, TSModule> entry: entries){
            String name = entry.getKey();
            childrenNames[i++] = name;
            byte[] nameBytes = name.getBytes();
            stream.write("import * as ".getBytes());
            stream.write(nameBytes);
            stream.write(" from \"./".getBytes());
            stream.write(nameBytes);
            stream.write("\";\n".getBytes());
            entry.getValue().finish();
        }
        stream.write("export { ".getBytes());
        stream.write(String.join(", ", childrenNames).getBytes());
        stream.write(" };".getBytes());
        stream.close();
    }
}
