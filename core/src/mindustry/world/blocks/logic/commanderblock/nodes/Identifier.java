package mindustry.world.blocks.logic.commanderblock.nodes;

import arc.struct.Array;
import mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class Identifier extends Node {
    public Array<Object> children = new Array<>(); //May be string or node
    public Identifier(){
        super();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private InterpreterObject parentObject;
        private InterpreterObject childObject;
        private Identifier node;
        private int index = 1;
        public Stepper(Identifier node, Interpreter interpreter){
            super(interpreter);
            this.node = node;
            childObject = InterpreterObject.create(node.children.get(0));
            parentObject = interpreter.scopes.peek();
            for(InterpreterObject scope: interpreter.scopes){
                if(scope.getProperty(childObject) != InterpreterObject.nullObject){
                    parentObject = scope;
                    break;
                }
            }
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            if(returnValue != null){
                parentObject = parentObject.getProperty(childObject);
                childObject = returnValue;
            }
            if(index < node.children.size){
                Object child = node.children.get(index++);
                if(child instanceof Node){
                    interpreter.stack.push((Node) child);
                } else {
                    interpreter.returnValue(InterpreterObject.create(child));
                }
                return false;
            }
            interpreter.returnValue(parentObject.getProperty(childObject));
            return true;
        }
    }
}
