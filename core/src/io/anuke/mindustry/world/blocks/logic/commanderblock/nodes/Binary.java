package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.arc.func.Func2;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;

public class Binary extends Node {
    public Node left;
    public Node right;
    public String operator;
    public Binary(){
        super();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return new Stepper(this, interpreter);
    }
    public static class Stepper extends Node.Stepper {
        private Binary node;
        private Object left;
        private Object right;
        private Func2<Object, Object, Object> operator;
        public Stepper(Binary node, Interpreter interpreter) {
            super(interpreter);
            this.node = node;
            operator = getOperator();
        }
        @Override
        public boolean step(InterpreterObject returnValue) {
            if(left == null){
                if(returnValue == null){
                    interpreter.stack.push(node.left);
                    return false;
                }
                left = returnValue.value();
                returnValue = null;
            }
            if(right == null){
                if(returnValue == null){
                    interpreter.stack.push(node.right);
                    return false;
                }
                right = returnValue.value();
            }
            interpreter.returnValue(InterpreterObject.create(operator.get(left, right)));
            return true;
        }
        private Func2<Object, Object, Object> getOperator(){
            switch (node.operator) {
                case "+": return (left, right) -> {
                    if(left instanceof Float && right instanceof Float){
                        return ((float) left) + ((float) right);
                    }
                    return throwOperatorOverloadError();
                };
                case "-": return (left, right) -> {
                    if(left instanceof Float && right instanceof Float){
                        return ((float) left) - ((float) right);
                    }
                    return throwOperatorOverloadError();
                };
                case "*": return (left, right) -> {
                    if(left instanceof Float && right instanceof Float){
                        return ((float) left) * ((float) right);
                    }
                    return throwOperatorOverloadError();
                };
                case "/": return (left, right) -> {
                    if(left instanceof Float && right instanceof Float){
                        return ((float) left) / ((float) right);
                    }
                    return throwOperatorOverloadError();
                };
                default:
                    throw new Interpreter.RuntimeError("Invalid operator \"" + node.operator + "\"!");
            }
        }
        private Void throwOperatorOverloadError(){
            throw new InterpreterObject.TypeError(
                    "No overload for the operator \"" + node.operator +
                    "\" accepts types \"" + left.getClass().getSimpleName() +
                    "\" and \"" + right.getClass().getSimpleName() +
                    "\"!"
            );
        }
    }
}
