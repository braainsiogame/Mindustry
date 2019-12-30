package io.anuke.mindustry.world.blocks.logic.commanderblock.parser;

public class Token {
    public Type type;
    public Object value;
    public int line;
    public int charStart;
    public int charEnd;
    public Token(Type type, Object value){
        this.type = type;
        this.value = value;
    }
    public Token metadata(int line, int charStart, int charEnd){
        this.line = line;
        this.charStart = charStart;
        this.charEnd = charEnd;
        return this;
    }
    public enum Type {
        punctuation,
        keyword,
        identifier,
        constant,
        operation
    }
    @Override
    public String toString(){
        return value.toString();
    }
}
