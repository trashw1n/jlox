public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int ln;

    Token(TokenType type, String lexeme, Object literal, int ln){
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.ln = ln;
    }
    public String toString(){
        return type + " " + lexeme + " " + literal;
    }
}
