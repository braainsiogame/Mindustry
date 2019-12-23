package io.anuke.mindustry.world.blocks.logic.commanderblock;

public class Parser {
    public static BoxedValue parseAny(CharStream charStream){
        if(charStream.peek() == '"'){ //Parse string
            charStream.next(); //Consume " character
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            while(charStream.peek() != '"' || escaped){
                char c = charStream.next();
                if(c == '\\' && !escaped){
                    escaped = true;
                } else {
                    if(escaped){
                        switch (c) {
                            case 'n':
                                c = '\n';
                                break;
                            case 'r':
                                c = '\r';
                                break;
                            case 'b':
                                c = '\b';
                                break;
                            case 't':
                                c = '\t';
                                break;
                        }
                    }
                    sb.append(c);
                    escaped = false;
                }
            }
            charStream.next(); //Consume " character
            return new BoxedValue(sb.toString());
        }
        if(Character.isDigit(charStream.peek()) || charStream.peek() == '-'){ //Parse character
            StringBuilder number = new StringBuilder();
            while(!charStream.finished() && (Character.isDigit(charStream.peek()) || charStream.peek() == '.' || charStream.peek() == '-')){
                number.append(charStream.next());
            }
            try {
                return new BoxedValue(Float.parseFloat(number.toString()));
            } catch(NumberFormatException e) {
                charStream.croak("Failed to parse number: " + e.getMessage() + "!");
            }
        }
        if(charStream.peek() == '$'){
            charStream.next();
            return new Variable(parseAny(charStream));
        }
        charStream.croak("Unexpected char '" + charStream.peek() + "'!");
        return null;
    }
    public static class CharStream {
        private int index = 0;
        private String string;
        private boolean finished;
        public CharStream(String string){
            this.string = string;
            finished = this.string.length() == 0;
        }
        public char peek(){
            if(finished) croak("Unexpected end of instruction!");
            return string.charAt(index);
        }
        public char next(){
            char value = peek();
            if(++index >= string.length()){
                finished = true;
            }
            return value;
        }
        public boolean finished(){
            return finished;
        }
        public int index(){
            return index;
        }
        public void croak(String msg) throws InstructionSyntaxError {
            throw new InstructionSyntaxError(msg);
        }
    }

    public static class InstructionSyntaxError extends Error {
        InstructionSyntaxError(String message) {
            super(message);
        }
    }
}
