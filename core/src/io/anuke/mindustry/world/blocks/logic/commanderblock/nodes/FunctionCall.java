package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;


import io.anuke.arc.collection.Array;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class FunctionCall extends Node {
    public Node func;
    public Array<Node> params;
    public FunctionCall(){
        super();
        func = null;
        params = new Array<>();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private FunctionCall node;
        private InterpreterObject[] params;
        private int index = 0;
        private boolean finished = false;
        public Stepper(FunctionCall node, Interpreter interpreter) {
            super(interpreter);
            this.node = node;
            params = new InterpreterObject[node.params.size];
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            if(finished) {
                interpreter.scopes.pop();
                interpreter.returnValue(returnValue);
                return true;
            }
            if(returnValue != null){
                if(index == params.length){
                    Object value = returnValue.value();
                    if(value instanceof FunctionExpression){
                        FunctionExpression funcExp = (FunctionExpression) value;
                        InterpreterObject scope = InterpreterObject.create();
                        int size = Math.min(params.length, funcExp.params.size);
                        for(int i = 0; i < size; i++){
                            scope.setProperty(
                                    InterpreterObject.create(funcExp.params.get(i)),
                                    params[i]
                            );
                        }
                        interpreter.scopes.push(scope);
                        interpreter.stack.push(funcExp.body);
                    } else if(value instanceof NativeFunction){
                        interpreter.scopes.push(InterpreterObject.create());
                        ((NativeFunction) value).func.get(params);
                    } else {
                        throw new InterpreterObject.TypeError("\"" + value + "\" is not a Function!");
                    }
                    finished = true;
                    return false;
                }
                params[index++] = returnValue;
            }
            interpreter.stack.push(index < params.length ? node.params.get(index) : node.func);
            return false;
        }
    }
}
