package io.anuke.mindustry.world.blocks.logic.commanderblock.nodes;

import io.anuke.arc.collection.Array;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;

public class Identifier extends Node {
    public Array<Object> children = new Array<>(); //May be string or node
    public Identifier(){
        super(Type.Identifier);
    }
    @Override
    public Stepper newStepper(Interpreter interpreter) {
        return null;
    }
}
