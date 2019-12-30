package mindustry.world.blocks.logic.commanderblock.nodes;

import mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class While extends Node {
    public Node condition;
    public Node body;
    public While(){
        super();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private While node;
        public Stepper(While node, Interpreter interpreter) {
            super(interpreter);
            this.node = node;
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            if(returnValue == null){
                interpreter.stack.push(node.condition);
                return false;
            }
            Object value = returnValue.value();
            if(value.equals(true) || value.equals(false)){
                if((boolean) value){
                    interpreter.stack.push(node.body);
                    return false;
                }
                return true;
            }
            throw new InterpreterObject.TypeError("\"" + value.getClass().getSimpleName() + "\" is not a Boolean!");
        }
    }
}
