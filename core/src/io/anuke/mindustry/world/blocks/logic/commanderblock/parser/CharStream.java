package io.anuke.mindustry.world.blocks.logic.commanderblock.parser;

public class CharStream {
    private int index = 0;
    private int lines = 0;
    private String string;
    private boolean finished;
    public CharStream(String string){
        this.string = string;
        finished = this.string.length() == 0;
    }
    public char peek(){
        if(finished) croak("Unexpected end of code!");
        char value = string.charAt(index);
        if(value == '\n') ++lines;
        return value;
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
    public int lines(){
        return lines;
    }
    public int chars(){
        return index;
    }
    public void croak(String msg) throws Parser.SyntaxError {
        throw new Parser.SyntaxError(msg);
    }
}