package io.anuke.mindustry.world.blocks.logic.commanderblock;

import io.anuke.mindustry.content.Fx;
import io.anuke.mindustry.entities.Effects;
import io.anuke.mindustry.world.blocks.logic.DroneCommanderBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

public class Instruction {
    private static HashMap<String, BiFunction<DroneCommanderBlock.DroneCommanderBlockEntity, BoxedValue[], Void>> stringToFunc;
    private BiFunction<DroneCommanderBlock.DroneCommanderBlockEntity, BoxedValue[], Void> func;
    private BoxedValue[] args;
    public boolean initOnly;
    public boolean alreadyRun;
    static {
        stringToFunc = new HashMap<>();
        stringToFunc.put("sleep", Instructions::sleep);
        stringToFunc.put("fx", Instructions::fx);
        stringToFunc.put("set", Instructions::set);
        stringToFunc.put("add", Instructions::add);
        stringToFunc.put("subtract", Instructions::subtract);
        stringToFunc.put("multiply", Instructions::multiply);
        stringToFunc.put("divide", Instructions::divide);
        stringToFunc.put("power", Instructions::power);
        stringToFunc.put("sqrt", Instructions::sqrt);
        stringToFunc.put("sin", Instructions::sin);
        stringToFunc.put("cos", Instructions::cos);
        stringToFunc.put("tan", Instructions::tan);
        stringToFunc.put("if", Instructions::if_);
        stringToFunc.put("subroutine", Instructions::subroutine);
        stringToFunc.put("goto", Instructions::goto_);
        stringToFunc.put("return", Instructions::return_);


    }
    public static Instruction parse(Parser.CharStream charStream) {
        ArrayList<BoxedValue> args = new ArrayList<>();
        StringBuilder nameBuilder = new StringBuilder();
        boolean initOnly = !charStream.finished() && charStream.peek() == '@';
        if(initOnly) charStream.next(); // Skip @
        while(!charStream.finished() && charStream.peek() != ' '){
            nameBuilder.append(charStream.next());
        }
        String name = nameBuilder.toString();
        BiFunction<DroneCommanderBlock.DroneCommanderBlockEntity, BoxedValue[], Void> func = stringToFunc.get(name);
        if(func == null){
            charStream.croak("No instruction named \"" + name + "\"!");
        }

        while(!charStream.finished()){
            charStream.next(); // Consume space
            args.add(Parser.parseAny(charStream));
        }

        return new Instruction(func, args.toArray(new BoxedValue[0]), initOnly);
    }
    private Instruction(BiFunction<DroneCommanderBlock.DroneCommanderBlockEntity, BoxedValue[], Void> func, BoxedValue[] args, boolean initOnly){
        this.func = func;
        this.args = args;
        this.initOnly = initOnly;
        alreadyRun = false;
    }
    public void run(DroneCommanderBlock.DroneCommanderBlockEntity entity){
        alreadyRun = true;
        func.apply(entity, args);
    }
    public static class InstructionError extends Error {
        public InstructionError(String message){
            super(message);
        }
    }
    public static class StackError extends InstructionError {
        public StackError(String message){
            super(message);
        }
    }
    public static class InvalidArgumentError extends InstructionError {
        public InvalidArgumentError(String message){
            super(message);
        }
    }
}