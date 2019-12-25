package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;


import io.anuke.arc.collection.Array;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;

public class FunctionCall extends Node {
    public Node func;
    public Array<Node> params;
    public FunctionCall(){
        super(Type.FunctionCall);
        func = null;
        params = new Array<>();
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return null;
    }
}
