package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;

public abstract class Node {
    public Type type;
    public Stepper stepper;
    protected Node(Type type){
        this.type = type;
    }
    public enum Type {
        Assignment,
        Binary,
        Codeblock,
        Constant,
        FunctionCall,
        FunctionExpression,
        Identifier,
        If
    }
    public abstract static class Stepper {
        protected Interpreter interpreter;
        public Stepper(Interpreter interpreter){
            this.interpreter = interpreter;
        }
        public abstract boolean step(); //Returns true if done
    }
    public abstract Stepper newStepper(Interpreter interpreter);
}
