package mindustry.world.blocks.logic.commanderblock.nodes;

import mindustry.world.blocks.logic.commanderblock.interpreter.*;

public class Assignment extends Node {
    public Identifier left;
    public Node right;
    public Assignment(){
        super();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private InterpreterObject parentObject;
        private InterpreterObject childObject;
        private Assignment node;
        private int index = 1;
        private boolean parsedLeft = false;
        public Stepper(Assignment node, Interpreter interpreter){
            super(interpreter);
            this.node = node;
            childObject = InterpreterObject.create(node.left.children.get(0));
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
                if(parsedLeft){
                    parentObject.setProperty(childObject, returnValue);
                    return true;
                }
                parentObject = parentObject.getProperty(childObject);
                childObject = returnValue;
            }
            if(index < node.left.children.size){
                Object child = node.left.children.get(index++);
                if(child instanceof Node){
                    interpreter.stack.push((Node) child);
                } else {
                    interpreter.returnValue(InterpreterObject.create(child));
                }
            } else {
                interpreter.stack.push(node.right);
                parsedLeft = true;
            }
            return false;
        }
    }
}