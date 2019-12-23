package io.anuke.mindustry.world.blocks.logic.commanderblock;

import io.anuke.arc.math.Mathf;
import io.anuke.arc.util.Log;
import io.anuke.mindustry.content.Fx;
import io.anuke.mindustry.entities.Effects;
import io.anuke.mindustry.world.blocks.logic.DroneCommanderBlock;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Instructions {
    public static final Instruction.InstructionError invalidArgumentsError = new Instruction.InvalidArgumentError("Invalid arguments!");
    public static Void sleep(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        if(args.length == 1){
            Object resolved = args[0].resolve(entity);
            if(resolved instanceof Float){
                entity.sleepCycles += Math.max((float) resolved, 0);
            } else {
                throw invalidArgumentsError;
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }
    public static Void fx(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        if(args.length == 2){
            Object x = args[0].resolve(entity);
            Object y = args[1].resolve(entity);
            if(x instanceof Float && y instanceof Float){
                Effects.effect(Fx.blastsmoke, (float) x, (float) y);
            } else {
                throw invalidArgumentsError;
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }
    public static Void set(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        if(args.length == 2 && args[0] instanceof Variable){
            Object key = ((Variable) args[0]).getKey(entity);
            Object value = args[1].resolve(entity);
            entity.variables.put(key, new BoxedValue(value));
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }
    public static Void add(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, Float::sum);
    }
    public static Void subtract(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, (a, b) -> a - b);
    }
    public static Void multiply(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, (a, b) -> a * b);
    }
    public static Void divide(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, (a, b) -> a / b);
    }
    public static Void power(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, Mathf::pow);
    }
    public static Void sqrt(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, Mathf::sqrt);
    }
    public static Void sin(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, Mathf::sinDeg);
    }
    public static Void cos(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, Mathf::cosDeg);
    }
    public static Void tan(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        return operator(entity, args, n -> (float) Math.tan(Math.toRadians(n)));
    }

    public static Void if_(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        if(args.length == 1){
            Object value = args[0].resolve(entity);
            boolean truthy = false;
            if(value instanceof Float){
                truthy = ((float) value) == 0f;
            } else if(value instanceof String){
                truthy = value.equals("");
            }
            if(truthy){
                ++entity.stack[entity.stackIndex];
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }

    public static Void subroutine(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        if(args.length == 1){
            Object key = args[0].resolve(entity);
            Integer index = entity.labels.get(key);
            if(index == null){
                //TODO throw error
            } else {
                if(entity.stackIndex + 1 >= entity.stack.length){
                    throw new Instruction.StackError("Stack overflow!");
                } else {
                    entity.stack[++entity.stackIndex] = index;
                }
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }

    public static Void goto_(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        if(args.length == 1){
            Object key = args[0].resolve(entity);
            Integer index = entity.labels.get(key);
            if(index == null){
                //TODO throw error
            } else {
                entity.stack[entity.stackIndex] = index;
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }
    public static Void return_(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args){
        if(args.length == 0){
            if(entity.stackIndex > 0){
                --entity.stackIndex;
            } else {
                throw new Instruction.StackError("Stack underflow!");
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }

    private static Void operator(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args, BiFunction<Float, Float, Float> op){
        if(args.length == 3 && args[2] instanceof Variable){
            Object a = args[0].resolve(entity);
            Object b = args[1].resolve(entity);
            if(a instanceof Float && b instanceof Float){
                Object key = ((Variable) args[2]).getKey(entity);
                entity.variables.put(key, new BoxedValue(op.apply((float) a, (float) b)));
            } else {
                throw invalidArgumentsError;
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }
    private static Void operator(DroneCommanderBlock.DroneCommanderBlockEntity entity, BoxedValue[] args, Function<Float, Float> op){
        if(args.length == 2 && args[1] instanceof Variable){
            Object n = args[0].resolve(entity);
            if(n instanceof Float){
                Object key = ((Variable) args[1]).getKey(entity);
                entity.variables.put(key, new BoxedValue(op.apply((float) n)));
            } else {
                throw invalidArgumentsError;
            }
        } else {
            throw invalidArgumentsError;
        }
        return null;
    }
}
