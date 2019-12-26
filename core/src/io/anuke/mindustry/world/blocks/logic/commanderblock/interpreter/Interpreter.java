package io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter;

import io.anuke.arc.math.Mathf;
import io.anuke.mindustry.world.blocks.logic.commanderblock.nodes.*;

import java.util.HashMap;
import java.util.Iterator;

public class Interpreter {
    public Stack<Node> stack = new Stack<>(new Node[1024]);
    public Stack<InterpreterObject> scopes = new Stack<>(new InterpreterObject[1024]);
    private InterpreterObject returnValue = null;
    public Interpreter(Node AST){
        stack.push(AST);
        scopes.push(createGlobalObject());
    }
    public void returnValue(InterpreterObject value){
        returnValue = value;
    }
    private InterpreterObject createGlobalObject(){
        InterpreterObject global = InterpreterObject.create();
        global.setProperty(InterpreterObject.create("Object"), InterpreterObject.create(0));
        global.setProperty(InterpreterObject.create("Math"), createMathObject());
        return global;
    }
    private InterpreterObject createMathObject(){
        InterpreterObject mathObj = InterpreterObject.create();
        NativeFunction sinFunc = new NativeFunction(args -> {
            if(args.length == 1){
                Object n = args[0].value();
                if(n instanceof Float){
                    returnValue(InterpreterObject.create(Mathf.sinDeg((float) n)));
                }
            }
            return null;
        });
        mathObj.setProperty(InterpreterObject.create("sin"), InterpreterObject.create(sinFunc));
        NativeFunction cosFunc = new NativeFunction(args -> {
            if(args.length == 1){
                Object n = args[0].value();
                if(n instanceof Float){
                    returnValue(InterpreterObject.create(Mathf.cosDeg((float) n)));
                }
            }
            return null;
        });
        mathObj.setProperty(InterpreterObject.create("cos"), InterpreterObject.create(cosFunc));
        return mathObj;
    }
    public InterpreterObject getValueFromScopeChain(InterpreterObject key){
        for(InterpreterObject scope: scopes){
            InterpreterObject value = scope.getProperty(key);
            if(value != InterpreterObject.nullObject){
                return value;
            }
        }
        return InterpreterObject.nullObject;
    }
    public boolean step(){
        Node node = stack.peek();
        if(node.stepper == null){
            node.stepper = node.newStepper(this);
        }
        InterpreterObject value = returnValue;
        returnValue = null;
        if(node.stepper.step(value)){
            node.stepper = null;
            stack.pop();
        }
        return stack.size() > 0;
    }
    public static class RuntimeError extends Error {
        public RuntimeError(String msg){
            super(msg);
        }
    }
    public static class Stack<T> implements Iterable<T> {
        private T[] array;
        private int index = -1;
        public Stack(T[] array){
            this.array = array;
        }
        public T peek(){
            return array[index];
        }
        public T pop(){
            if(index == -1) throw new StackError("Stack underflow!");
            T value = array[index];
            array[index--] = null;
            return value;
        }
        public T push(T element){
            if(++index == array.length) throw new StackError("Stack overflow!");
            array[index] = element;
            return element;
        }
        public int size(){
            return index + 1;
        }
        public T[] array(){
            return array;
        }
        public Iterator<T> iterator(){
            return new Iterator<T>() {
                private int i = index;
                @Override
                public boolean hasNext() {
                    return i >= 0;
                }
                @Override
                public T next() {
                    return array[i--];
                }
            };
        }
        private static class StackError extends Interpreter.RuntimeError{
            public StackError(String msg) {
                super(msg);
            }
        }
    }
}
