package io.anuke.mindustry.world.blocks.logic.commanderblock.parser;

import io.anuke.arc.collection.Array;
import io.anuke.arc.func.Func;
import io.anuke.mindustry.world.blocks.logic.commanderblock.nodes.*;

public class Parser {
    private static TokenStream stream;
    public static Codeblock parse(TokenStream tokenStream){
        stream = tokenStream;
        Codeblock block = new Codeblock();
        while (!stream.finished()) {
            block.body.add(parseExpression());
            if (!stream.finished()) skipPunctuation(';');
        }
        stream = null;
        return block;
    }
    private static Node parseExpression(){
        return mayBeFunctionCall(stream -> mayBeBinary(parseAtom(), 0));
    }
    private static Node mayBeFunctionCall(Func<TokenStream, Node> expr){
        Node result = expr.get(stream);
        return isPunctuation('(') ? parseFunctionCall(result) : result;
    }
    private static Node mayBeBinary(Node left, int precedence){
        Token token = stream.peek();
        if (token.type == Token.Type.operation) {
            int otherPrecedence = getPrecedence((String) token.value);
            if (otherPrecedence > precedence) {
                stream.next();
                Node right = mayBeBinary(parseAtom(), otherPrecedence);
                Node binary = null;
                if(token.value.equals("=")){
                    Assignment assignment = new Assignment();
                    if(left instanceof Identifier){
                        assignment.left = (Identifier) left;
                        assignment.right = right;
                        binary = assignment;
                    } else {
                        unexpected("an identifier");
                    }
                } else {
                    Binary bin = new Binary();
                    bin.operator = (String) token.value;
                    bin.left = left;
                    bin.right = right;
                    binary = bin;
                }
                return mayBeBinary(binary, precedence);
            }
        }
        return left;
    }

    private static Node parseAtom() {
        return mayBeFunctionCall(stream -> {
            if (isPunctuation('(')) {
                stream.next();
                Node expression = parseExpression();
                skipPunctuation(')');
                return expression;
            }
            if (isPunctuation('{')) return parseBlock();
            if (isKeyword("if")) return parseIf();
            if (isKeyword("func")) return parseFunctionExpression();
            Token.Type tokenType = stream.peek().type;
            if (tokenType == Token.Type.identifier) return parseIdentifier();
            if (tokenType == Token.Type.constant) return parseConstant();
            unexpected(null);
            return null;
        });
    }
    private static Constant parseConstant(){
        Token token = stream.next();
        Constant constant = new Constant();
        constant.value = token.value;
        return constant;
    }
    private static Identifier parseIdentifier(){
        Token token = stream.next();
        Identifier identifier = new Identifier();
        identifier.children.add(token.value);
        while(isPunctuation('.') || isPunctuation('[')){
            if((char) stream.next().value == '.'){
                if(stream.peek().type != Token.Type.identifier){
                    unexpected("an identifier");
                }
                Token child = stream.next();
                identifier.children.add(child.value);
            } else {
                identifier.children.add(parseExpression());
                skipPunctuation(']');
            }
        }
        return identifier;
    }
    private static Node parseBlock() {
        Codeblock block = new Codeblock();
        block.body = parseDelimited('{', '}', ';', stream -> parseExpression());
        return block;
    }

    private static int getPrecedence(String operator){
        switch (operator) {
            case "=":
                return 1;
            case "||":
                return 2;
            case "&&":
                return 3;
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
                return 7;
            case "+":
            case "-":
                return 10;
            case "*":
            case "/":
            case "%":
                return 20;
            default:
                return 0;
        }
    }
    private static FunctionExpression parseFunctionExpression(){
        skipKeyword("func");
        FunctionExpression funcExp = new FunctionExpression();
        funcExp.params = parseDelimited('(', ')', ',', stream -> {
            if(stream.peek().type != Token.Type.identifier){
                unexpected(null);
            }
            return (String) stream.next().value;
        });
        funcExp.body = parseExpression();
        return funcExp;
    }
    private static FunctionCall parseFunctionCall(Node result) {
        FunctionCall call = new FunctionCall();
        call.func = result;
        call.params = parseDelimited('(', ')', ',', stream -> parseExpression());
        return call;
    }
    private static If parseIf() {
        If ret = new If();
        skipKeyword("if");
        skipPunctuation('(');
        ret.condition = parseExpression();
        skipPunctuation(')');
        ret.body = parseExpression();
        if(isKeyword("else")){
            stream.next();
            ret.elseBody = parseExpression();
        }
        return ret;
    }
    //Parse sequences like (0, 1, 2)
    private static <T> Array<T> parseDelimited(char start, char end, char separator, Func<TokenStream, T> parser){
        Array<T> nodes = new Array<>();
        skipPunctuation(start);
        boolean firstElement = true;
        while (!stream.finished()) {
            if (isPunctuation(end)) break;
            if (firstElement) {
                firstElement = false;
            } else {
                skipPunctuation(separator);
            }
            if (isPunctuation(end)) break; // the last separator can be missing
            nodes.add(parser.get(stream));
        }
        skipPunctuation(end);
        return nodes;
    }
    private static void skipKeyword(String keyword){
        skip(Token.Type.keyword, keyword, v -> "keyword \"" + keyword + '"');
    }
    private static boolean isKeyword(String keyword){
        return is(Token.Type.keyword, keyword);
    }
    private static void skipPunctuation(char punc){
        skip(Token.Type.punctuation, punc, v -> "char '" + punc + "'");
    }
    private static boolean isPunctuation(char punc){
        return is(Token.Type.punctuation, punc);
    }
    private static boolean isOperation(String op){
        return is(Token.Type.operation, op);
    }
    //Skips if the current token matches the input type and value, errors otherwise
    private static void skip(Token.Type type, Object value, Func<Void, String> err){
        if(is(type, value)){
            stream.next();
        } else {
            unexpected(err.get(null));
        }
    }
    //Returns true if the current token matches the input type and value
    private static boolean is(Token.Type type, Object value){
        if(stream.finished()) return false;
        Token token = stream.peek();
        return token.type == type && value.equals(token.value);
    }
    private static void unexpected(String err){
        StringBuilder msg = new StringBuilder("Unexpected token \"");
        msg.append(stream.peek().toString());
        msg.append("\"!");
        if(err != null){
            msg.append(" Expected ");
            msg.append(err);
            msg.append(".");
        }
        stream.croak(msg.toString());
    }
    public static class SyntaxError extends Error{
        public SyntaxError(String msg){
            super(msg);
        }
    }
}
