package io.anuke.mindustry.world.blocks.logic.commanderblock;

import io.anuke.arc.math.Mathf;
import io.anuke.mindustry.content.Fx;
import io.anuke.mindustry.entities.Effects;
import io.anuke.mindustry.world.blocks.logic.DroneCommanderBlock;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Globals;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;
import io.anuke.mindustry.world.blocks.logic.commanderblock.nodes.NativeFunction;

public class BlockGlobals {
    public abstract static class BlockGlobal extends Globals.Global {
        public DroneCommanderBlock.DroneCommanderBlockEntity entity;
        public BlockGlobal(Interpreter interpreter, DroneCommanderBlock.DroneCommanderBlockEntity entity) {
            super(interpreter);
            this.entity = entity;
        }
    }
    public static void modifyGlobals(Interpreter interpreter, DroneCommanderBlock.DroneCommanderBlockEntity entity, InterpreterObject global){
        global.setProperty(InterpreterObject.create("Debug"), new Debug_(interpreter, entity).global());
        global.setProperty(InterpreterObject.create("Block"), new Block_(interpreter, entity).global());
    }
    public static class Debug_ extends BlockGlobal {
        public Debug_(Interpreter interpreter, DroneCommanderBlock.DroneCommanderBlockEntity entity) {
            super(interpreter, entity);
        }
        public InterpreterObject global(){
            InterpreterObject obj = InterpreterObject.create();
            obj.setProperty(InterpreterObject.create("effect"), InterpreterObject.create(new NativeFunction(this::effect)));
            return obj;
        }
        public InterpreterObject effect(InterpreterObject[] args){
            if(args.length == 2){
                Object x = args[0].value();
                Object y = args[1].value();
                if(x instanceof Float && y instanceof Float){
                    Effects.effect(Fx.blastsmoke, (float) x, (float) y);
                }
            }
            return InterpreterObject.nullObject;
        }
    }
    public static class Block_ extends BlockGlobal {
        public Block_(Interpreter interpreter, DroneCommanderBlock.DroneCommanderBlockEntity entity) {
            super(interpreter, entity);
        }
        public InterpreterObject global(){
            InterpreterObject obj = InterpreterObject.create();
            obj.setProperty(InterpreterObject.create("sleep"), InterpreterObject.create(new NativeFunction(this::sleep)));
            obj.setProperty(InterpreterObject.create("pos"), positionObject());
            return obj;
        }
        private InterpreterObject positionObject(){
            InterpreterObject pos = InterpreterObject.create();
            pos.setProperty(InterpreterObject.create("x"), InterpreterObject.create(entity.x));
            pos.setProperty(InterpreterObject.create("y"), InterpreterObject.create(entity.y));
            return pos;
        }
        public InterpreterObject sleep(InterpreterObject[] args){
            entity.sleepCycles = 1;
            if(args.length > 0){
                Object value = args[0].value();
                if(value instanceof Float) {
                    entity.sleepCycles = Mathf.floorPositive((float) value);
                }
            }
            if(entity.sleepCycles < 0){
                entity.sleepCycles = 0;
            }
            return InterpreterObject.nullObject;
        }
    }
}
