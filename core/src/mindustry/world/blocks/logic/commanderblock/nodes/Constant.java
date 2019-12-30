package mindustry.world.blocks.logic.commanderblock.nodes;

import mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class Constant extends Node {
    public Object value;
    public Constant(){
        super();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private InterpreterObject value;
        public Stepper(Constant node, Interpreter interpreter) {
            super(interpreter);
            value = InterpreterObject.create(node.value);
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            interpreter.returnValue(value);
            return true;
        }
    }
}
