package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.arc.collection.Array;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;

public class FunctionExpression extends Node {
    public Node body;
    public Array<String> params;
    public FunctionExpression(){
        super(Type.FunctionExpression);
        params = new Array<>();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return null;
    }
}
