package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.arc.collection.Array;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class FunctionExpression extends Node {
    public Node body;
    public Array<String> params;
    public InterpreterObject[] scopes;
    public FunctionExpression(){
        super();
        params = new Array<>();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private InterpreterObject value;
        public Stepper(FunctionExpression node, Interpreter interpreter) {
            super(interpreter);
            this.value = InterpreterObject.create(node);
            node.scopes = new InterpreterObject[interpreter.scopes.size()];
            InterpreterObject[] scopes = interpreter.scopes.array();
            System.arraycopy(scopes, 0, node.scopes, 0, node.scopes.length);
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            interpreter.returnValue(value);
            return true;
        }
    }
}
