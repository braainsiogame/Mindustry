package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.arc.collection.Array;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

import java.util.Iterator;

public class Codeblock extends Node {
    public Array<Node> body;
    public Codeblock(){
        super();
        body = new Array<>();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private Iterator<Node> body;
        public Stepper(Codeblock node, Interpreter interpreter) {
            super(interpreter);
            body = node.body.iterator();
            interpreter.scopes.push(InterpreterObject.create());
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            if(body.hasNext()){
                interpreter.stack.push(body.next());
                return false;
            }
            interpreter.scopes.pop();
            return true;
        }
    }
}
