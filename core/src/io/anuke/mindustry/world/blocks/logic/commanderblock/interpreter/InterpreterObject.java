package io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter;

import java.util.HashMap;

public class InterpreterObject {
    public static final InterpreterObject nullObject = new InterpreterObject();
    private Object value;
    private HashMap<Object, InterpreterObject> map;
    private InterpreterObject(){

    }
    public static InterpreterObject create(Object value){
        InterpreterObject obj = new InterpreterObject();
        obj.value = value;
        return value == null ? nullObject : obj;
    }
    public static InterpreterObject create(){
        InterpreterObject obj = new InterpreterObject();
        obj.map = new HashMap<>();
        return obj;
    }

    public Object value(){
        return value == null ? this : value;
    }
    public void setProperty(InterpreterObject key, InterpreterObject value){
        throwIfSelfIsNullObject(key);
        map.put(key.value(), value);
    }
    public InterpreterObject getProperty(InterpreterObject key){
        throwIfSelfIsNullObject(key);
        InterpreterObject value = map.get(key.value());
        return value == null ? InterpreterObject.nullObject : value;
    }
    private void throwIfSelfIsNullObject(InterpreterObject key){
        if(this == nullObject){
            throw new TypeError("Can't access property \"" + key.value() + "\" of null.");
        }
    }
    public static class TypeError extends Interpreter.RuntimeError {
        public TypeError(String msg) {
            super(msg);
        }
    }
}
