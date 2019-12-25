package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;

public class If extends Node {
    public Node condition;
    public Node body;
    public Node elseBody;
    public If(){
        super(Type.If);
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return null;
    }
}
