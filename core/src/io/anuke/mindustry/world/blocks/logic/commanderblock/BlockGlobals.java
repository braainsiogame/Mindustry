package io.anuke.mindustry.world.blocks.logic.commanderblock;

import io.anuke.arc.math.Mathf;
import io.anuke.mindustry.content.Fx;
import io.anuke.mindustry.entities.Effects;
import io.anuke.mindustry.world.blocks.logic.DroneCommanderBlock;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Globals;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.Interpreter;
import io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter.InterpreterObject;
import io.anuke.mindustry.world.blocks.logic.commanderblock.nodes.NativeFunction;

import java.lang.ref.WeakReference;

public class BlockGlobals {
    public static void modifyGlobals(Interpreter interpreter, DroneCommanderBlock.DroneCommanderBlockEntity entity, InterpreterObject global){
        global.setProperty(InterpreterObject.create("Debug"), new Debug_(interpreter).global());
        global.setProperty(InterpreterObject.create("CPU"), CPU_.global());
    }
    public static class Debug_ {
        private Interpreter interpreter;
        private DroneCommanderBlock.DroneCommanderBlockEntity entity;
        public Debug_(Interpreter interpreter){
            this.interpreter = interpreter;
        }
        public InterpreterObject global(BlockGlobals globals){
            InterpreterObject obj = InterpreterObject.create();
            obj.setProperty(InterpreterObject.create("effect"), InterpreterObject.create(new NativeFunction(Debug_::effect)));
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
    public static class CPU_ {
        public InterpreterObject global(){
            InterpreterObject obj = InterpreterObject.create();
            obj.setProperty(InterpreterObject.create("sleep"), InterpreterObject.create(new NativeFunction(CPU_::sleep)));
            return obj;
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
