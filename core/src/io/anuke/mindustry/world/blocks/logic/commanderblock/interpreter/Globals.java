package io.anuke.mindustry.world.blocks.logic.commanderblock.interpreter;

import io.anuke.arc.func.Func;
import io.anuke.arc.func.Func2;
import io.anuke.arc.math.Mathf;
import io.anuke.mindustry.world.blocks.logic.commanderblock.nodes.NativeFunction;

public class Globals {
    public abstract static class Global{
        protected Interpreter interpreter;
        public Global(Interpreter interpreter){
            this.interpreter = interpreter;
        }
    }
    public static void modifyGlobals(InterpreterObject global, Interpreter interpreter){
        global.setProperty(InterpreterObject.create("Object"), new Object_(interpreter).global());
        global.setProperty(InterpreterObject.create("Math"), new Math_(interpreter).global());
        global.setProperty(InterpreterObject.create("List"), new List_(interpreter).global());
    }
    public static class Math_ extends Global {
        public Math_(Interpreter interpreter) {
            super(interpreter);
        }
        public InterpreterObject global(){
            InterpreterObject obj = InterpreterObject.create();
            obj.setProperty(InterpreterObject.create("sin"), InterpreterObject.create(new NativeFunction(this::sin)));
            obj.setProperty(InterpreterObject.create("cos"), InterpreterObject.create(new NativeFunction(this::cos)));
            obj.setProperty(InterpreterObject.create("tan"), InterpreterObject.create(new NativeFunction(this::tan)));
            obj.setProperty(InterpreterObject.create("PI"), InterpreterObject.create(Math.PI));
            obj.setProperty(InterpreterObject.create("E"), InterpreterObject.create(Math.E));
            obj.setProperty(InterpreterObject.create("round"), InterpreterObject.create(new NativeFunction(this::round)));
            obj.setProperty(InterpreterObject.create("random"), InterpreterObject.create(new NativeFunction(this::random)));
            obj.setProperty(InterpreterObject.create("pow"), InterpreterObject.create(new NativeFunction(this::pow)));
            return obj;
        }
        public InterpreterObject sin(InterpreterObject[] args){
            return mathFunc(args, Mathf::sin);
        }
        public InterpreterObject cos(InterpreterObject[] args){
            return mathFunc(args, Mathf::cos);
        }
        public InterpreterObject tan(InterpreterObject[] args){
            return mathFunc(args, n -> (float) Math.tan(n));
        }
        public InterpreterObject round(InterpreterObject[] args){
            return mathFunc(args, n -> (float) Mathf.round(n));
        }
        public InterpreterObject random(InterpreterObject[] args){
            return mathFunc(args, Mathf::random);
        }
        public InterpreterObject pow(InterpreterObject[] args){
            return mathFunc2(args, Mathf::pow);
        }
        private InterpreterObject mathFunc(InterpreterObject[] args, Func<Float, Float> op){
            if(args.length == 1){
                Object n = args[0].value();
                if(n instanceof Float){
                    return InterpreterObject.create(op.get((float) n));
                }
            }
            return InterpreterObject.nullObject;
        }
        private InterpreterObject mathFunc2(InterpreterObject[] args, Func2<Float, Float, Float> op){
            if(args.length == 2){
                Object a = args[0].value();
                Object b = args[1].value();
                if(a instanceof Float && b instanceof Float){
                    return InterpreterObject.create(op.get((float) a, (float) b));
                }
            }
            return InterpreterObject.nullObject;
        }
    }
    public static class Object_ extends Global {
        public Object_(Interpreter interpreter) {
            super(interpreter);
        }
        public InterpreterObject global(){
            InterpreterObject obj = InterpreterObject.create();
            obj.setProperty(InterpreterObject.create("create"), InterpreterObject.create(new NativeFunction(this::create)));
            return obj;
        }
        public InterpreterObject create(InterpreterObject[] args){
            return InterpreterObject.create();
        }
    }
    public static class List_ extends Global {
        private static InterpreterObject listLength = InterpreterObject.create("length");
        private static InterpreterObject listPush = InterpreterObject.create("push");
        private static InterpreterObject listPop = InterpreterObject.create("pop");
        private static InterpreterObject listRemove = InterpreterObject.create("remove");
        private static InterpreterObject listInsert = InterpreterObject.create("insert");
        public List_(Interpreter interpreter) {
            super(interpreter);
        }
        public InterpreterObject global(){
            InterpreterObject obj = InterpreterObject.create();
            obj.setProperty(InterpreterObject.create("create"), InterpreterObject.create(new NativeFunction(this::create)));
            return obj;
        }
        public InterpreterObject create(InterpreterObject[] _args){
            InterpreterObject list = InterpreterObject.create();
            list.setProperty(listLength, InterpreterObject.create(0f));
            NativeFunction listPushFunc = new NativeFunction(args -> {
                Object obj = list.getProperty(listLength).value();
                if(obj instanceof Float){
                    int index = Mathf.floor((float) obj);
                    for(InterpreterObject arg: args){
                        list.setProperty(InterpreterObject.create((float) index++), arg);
                    }
                    list.setProperty(listLength, InterpreterObject.create((float) index));
                }
                return list;
            });
            list.setProperty(listPush, InterpreterObject.create(listPushFunc));
            NativeFunction listPopFunc = new NativeFunction(args -> {
                Object obj = list.getProperty(listLength).value();
                if(obj instanceof Float){
                    int index = Mathf.floor((float) obj);
                    list.removeProperty(InterpreterObject.create((float) --index));
                    list.setProperty(listLength, InterpreterObject.create((float) index));
                }
                return list;
            });
            list.setProperty(listPop, InterpreterObject.create(listPopFunc));
            return list;
        }
        private InterpreterObject indexFunc(InterpreterObject[] args, Func<Integer, InterpreterObject> op){
            if(args.length == 1){
                Object n = args[0].value();
                if(n instanceof Float){
                    return op.get(Mathf.floor((float) n));
                }
            }
            return InterpreterObject.nullObject;
        }
    }
}
