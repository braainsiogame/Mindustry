package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public abstract class Node {
    public Stepper stepper;
    protected Node(){

    }
    public abstract static class Stepper {
        protected Interpreter interpreter;
        public Stepper(Interpreter interpreter){
            this.interpreter = interpreter;
        }
        public abstract boolean step(InterpreterObject returnValue); //Returns true if done
    }
    public abstract Stepper newStepper(Interpreter interpreter);
}
