import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }
    private final String src;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0, curr = 0, ln = 1;
    Scanner(String src){
        this.src = src;
    }

    List<Token> scanTokens(){
        while(!isAtEnd()){
            start = curr;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, ln));
        return tokens;
    }

    private boolean isAtEnd(){
        return curr >= src.length();
    }

    private void scanToken(){
        char ch = advance();
        switch(ch){
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!': addToken(match('=')? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=')? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=')? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=')? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '/':
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                }
                else addToken(TokenType.SLASH);
                break;
            //ignore whitespace and other similar characters
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n': ln++; break;
            case '"': string(); break;
            default:
                if(isDigit(ch)) number();
                else if(isAlpha(ch)) identifier();
                else Lox.error(ln, "Unexpected character."); break;
        }
    }

    private char advance(){
        return src.charAt(curr++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String txt = src.substring(start, curr);
        tokens.add(new Token(type, txt, literal, ln));
    }

    private boolean match(char expected){
        if(isAtEnd()) return false;
        if(src.charAt(curr) != expected) return false;
        curr++;
        return true;
    }

    private char peek(){
        if(isAtEnd()) return '\0';
        return src.charAt(curr);
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') ln++;
            advance();
        }
        if(isAtEnd()){
            Lox.error(ln, "Unterminated string.");
            return;
        }
        advance();
        String val = src.substring(start+1, curr-1);
        addToken(TokenType.STRING, val);
    }

    private boolean isDigit(char ch){
        return ch >= '0' && ch <= '9';
    }

    private void number(){
        while(isDigit(peek())) advance();
        if(peek() == '.' && isDigit(peekNext())){
            advance();
            while(isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(src.substring(start, curr)));
    }

    private char peekNext(){
        if(curr+1 >= src.length()) return '\0';
        return src.charAt(curr+1);
    }

    private boolean isAlpha(char ch){
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch == '_');
    }

    private boolean isAlphaNumeric(char ch){
        return isAlpha(ch) || isDigit(ch);
    }

    private void identifier(){
        while(isAlphaNumeric(peek())) advance();
        String txt = src.substring(start, curr);
        TokenType type = keywords.get(txt);
        if(type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }


}
