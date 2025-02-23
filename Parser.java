import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int curr = 0;
    private static class ParseError extends RuntimeException{

    }
    Parser(List<Token> tokens){
        this.tokens = tokens;
    }
    // Expr parse(){
    //     try{
    //         return expression();
    //     } catch(ParseError error){
    //         return null;
    //     }   
    // }
    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) statements.add(declaration());
        return statements;
    }
    private Stmt declaration(){
        try{
            if(match(TokenType.VAR)) return varDeclaration();
            return statement();
        } catch(ParseError e){
            synchronize();
            return null;
        }
    }
    private Stmt varDeclaration(){
        Token name = consume(TokenType.IDENTIFIER, "Exprected variable name.");
        Expr initializer = null;
        if(match(TokenType.EQUAL)) initializer = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer); 
    }
    private Stmt statement(){
        if(match(TokenType.IF)) return ifStatement();
        if(match(TokenType.WHILE)) return whileStatement();
        if(match(TokenType.PRINT)) return printStatement();
        if(match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }
    private Stmt whileStatement(){
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected closing ')' after condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }
    private Stmt printStatement(){
        Expr val = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(val);
    }
    private Stmt ifStatement(){
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect closing ')' after 'if'.");
        Stmt thenBranch = statement(), elseBranch = null;
        if(match(TokenType.ELSE)) elseBranch = statement();
        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()) statements.add(declaration());
        consume(TokenType.RIGHT_BRACE, "Expected terminating '}' after block.");
        return statements;
    }
    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }
    private Expr expression(){
        return assignment();
    }
    private Expr assignment(){
        Expr lval = or();
        if(match(TokenType.EQUAL)){
            Token equals = previous();
            Expr rval = assignment();
            if(lval instanceof Expr.Variable){
                Token name = ((Expr.Variable)lval).name;
                return new Expr.Assign(name, rval);
            }
            error(equals, "Invalid assignment target.");    
        }
        //return parsed non-assignment expression
        return lval;
    }
    private Expr or(){
        Expr l = and();
        while(match(TokenType.OR)){
            Token op = previous();
            Expr r = and();
            l = new Expr.Logical(l, op, r);
        }
        return l;
    }
    private Expr and(){
        Expr l = equality();
        while(match(TokenType.AND)){
            Token op = previous();
            Expr r = equality();
            l = new Expr.Logical(l, op, r);
        }
        return l;
    }
    private Expr equality(){ 
        Expr expr = comparison();
        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)){
            Token op = previous();
            Expr r = comparison();
            expr = new Expr.Binary(expr, op, r);
        }
        return expr;
    }
    private Expr comparison(){
        Expr expr = term();
        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)){
            Token op = previous();
            Expr r = term();
            expr = new Expr.Binary(expr, op, r);
        }
        return expr;
    }
    private Expr term(){
        Expr expr = factor();
        while(match(TokenType.MINUS, TokenType.PLUS)){
            Token op = previous();
            Expr r = factor();
            expr = new Expr.Binary(expr, op, r);
        }
        return expr;
    }
    private Expr factor(){
        Expr expr = unary();
        while(match(TokenType.SLASH, TokenType.STAR)){
            Token op = previous();
            Expr r = unary();
            expr = new Expr.Binary(expr, op, r);
        }
        return expr;
    }
    private Expr unary(){
        if(match(TokenType.BANG, TokenType.MINUS)){
            Token op = previous();
            Expr r = unary();
            return new Expr.Unary(op, r);
        }
        return primary();
    }
    private Expr primary(){
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);
        if(match(TokenType.NUMBER, TokenType.STRING)){
            return new Expr.Literal(previous().literal);
        }
        if(match(TokenType.IDENTIFIER)) return new Expr.Variable(previous());
        if(match(TokenType.LEFT_PAREN)){
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression");
    }
    private boolean match(TokenType... types){
        for(TokenType type: types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }
    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().type == type;
    }
    private Token advance(){
        if(!isAtEnd()) curr++;
        return previous();
    }
    private boolean isAtEnd(){
        return peek().type == TokenType.EOF;
    }
    private Token peek(){
        return tokens.get(curr);
    }
    private Token previous(){
        return tokens.get(curr-1);
    }
    private Token consume(TokenType type, String msg){
        if(check(type)) return advance();
        throw error(peek(), msg);
    }
    private ParseError error(Token token, String msg){
        Lox.error(token, msg);
        return new ParseError();
    }
    private void synchronize(){
        advance();
        while(!isAtEnd()){
            if(previous().type == TokenType.SEMICOLON) return;
            switch(peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }

    }
}   