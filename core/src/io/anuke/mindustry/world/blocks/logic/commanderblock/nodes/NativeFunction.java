package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.arc.func.Func;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class NativeFunction extends Node{
    public Func<InterpreterObject[], InterpreterObject> func;
    public NativeFunction(Func<InterpreterObject[], InterpreterObject> func){
        super();
        this.func = func;
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private InterpreterObject value;
        public Stepper(NativeFunction node, Interpreter interpreter) {
            super(interpreter);
            this.value = InterpreterObject.create(node);
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            interpreter.returnValue(value);
            return true;
        }
    }
}
