package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class If extends Node {
    public Node condition;
    public Node body;
    public Node elseBody;
    public If(){
        super();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private If node;
        private boolean finished = false;
        public Stepper(If node, Interpreter interpreter) {
            super(interpreter);
            this.node = node;
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            if(finished) return true;
            if(returnValue == null){
                interpreter.stack.push(node.condition);
                return false;
            }
            Object value = returnValue.value();
            if(value instanceof Boolean){
                interpreter.stack.push((boolean) value ? node.body : node.elseBody);
            } else {
                throw new InterpreterObject.TypeError("\"" + value.getClass().getSimpleName() + "\" is not a Boolean!");
            }
            finished = true;
            return false;
        }
    }
}
