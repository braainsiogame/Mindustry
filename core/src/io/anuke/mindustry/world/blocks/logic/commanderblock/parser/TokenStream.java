package io.anuke.mindustry.world.blocks.logic.commanderblock.parser;

import io.anuke.arc.func.Func;

import java.util.concurrent.atomic.AtomicBoolean;

public class TokenStream {
    private CharStream charStream;
    private Token current;
    public TokenStream(CharStream charStream){
        this.charStream = charStream;
        current = null;
    }
    private boolean isWhitespace(char c){
        return Character.isWhitespace(c);
    }
    private boolean isIdentifierStart(char c){
        return Character.isLetter(c) || c == '_';
    }
    private boolean isIdentifier(char c){
        return Character.isLetterOrDigit(c) || c == '_';
    }
    private boolean isKeyword(String value) {
        switch(value){
            case "if":
            case "else":
            case "func":
            case "while":
            case "for":
                return true;
            default:
                return false;
        }
    }
    private Token readIdentifier(){
        int line = charStream.lines();
        int start = charStream.chars();
        String value = readWhile(this::isIdentifier);
        int end = charStream.chars();
        if(value.equals("true") || value.equals("false")){
            return new Token(Token.Type.constant, value.equals("true")).metadata(line, start, end);
        }
        return new Token(isKeyword(value) ? Token.Type.keyword : Token.Type.identifier, value).metadata(line, start, end);
    }


    private boolean isDigit(char c){
        return Character.isDigit(c);
    }
    private Token readNumber(){
        int lines = charStream.lines();
        int start = charStream.chars();
        AtomicBoolean hasDot = new AtomicBoolean(false);
        String number = readWhile(c -> {
            if(c == '.'){
                if(hasDot.get()) return false;
                hasDot.set(true);
                return true;
            }
            return isDigit(c);
        });
        try {
            return new Token(Token.Type.constant, Float.parseFloat(number))
                    .metadata(lines, start, charStream.chars());
        } catch(NumberFormatException e) {
            charStream.croak("Failed to parse number: " + e.getMessage() + "!");
            return null;
        }
    }
    private boolean isStringStart(char ch){
        return ch == '"';
    }
    private Token readString(){
        int line = charStream.lines();
        int start = charStream.chars();
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
        return new Token(Token.Type.constant, sb.toString()).metadata(line, start, charStream.chars());
    }
    private boolean isOperationChar(char c){
        switch (c){
            case '>':
            case '<':
            case '=':
            case '+':
            case '*':
            case '-':
            case '/':
            case '%':
            case '&':
            case '|':
            case '!':
                return true;
            default:
                return false;
        }
    }
    private boolean isPunctuation(char c){
        switch (c){
            case ',':
            case ';':
            case '.':
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
                return true;
            default:
                return false;
        }
    }
    private void skipComment() {
        readWhile(ch -> ch != '\n');
        charStream.next();
    }
    private String readWhile(Func<Character, Boolean> predicate){
        StringBuilder sb = new StringBuilder();
        while(!charStream.finished() && predicate.get(charStream.peek())){
            sb.append(charStream.next());
        }
        return sb.toString();
    }
    private Token readToken(){
        readWhile(this::isWhitespace);
        char ch = charStream.peek();
        if (ch == '#') {
            skipComment();
            return readToken();
        }
        if (isStringStart(ch)) return readString();
        if (isDigit(ch)) return readNumber();
        if (isIdentifierStart(ch)) return readIdentifier();
        if (isPunctuation(ch)) return new Token(Token.Type.punctuation, charStream.next())
                .metadata(charStream.lines(), charStream.chars() - 1, charStream.chars());
        if (isOperationChar(ch)) {
            int line = charStream.lines();
            int start = charStream.chars();
            return new Token(Token.Type.operation, readWhile(this::isOperationChar))
                    .metadata(line, start, charStream.chars());
        }
        charStream.croak("Unexpected char '" + ch + "'!");
        return null;
    }
    public Token peek(){
        if(current == null) current = readToken();
        return current;
    }
    public Token next(){
        Token value = peek();
        current = null;
        return value;
    }
    public boolean finished(){
        return charStream.finished() && current == null;
    }
    public void croak(String msg){
        charStream.croak(msg);
    }
}
