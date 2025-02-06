public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr){
        return expr.accept(this);
    }
    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return parenthesize(expr.op.lexeme, expr.l, expr.r);
    }
    @Override
    public String visitGroupingExpr(Expr.Grouping expr){
        return parenthesize("group", expr.expr);
    }
    @Override
    public String visitLiteralExpr(Expr.Literal expr){
        if(expr.val == null) return "nil";
        return expr.val.toString();
    }
    @Override
    public String visitUnaryExpr(Expr.Unary expr){
        return parenthesize(expr.op.lexeme, expr.r);
    }

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for(Expr expr: exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();  
    }
    public static void main(String[] args) {
        Expr expr = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1), 
                new Expr.Literal(123)
            ), 
            new Token(TokenType.STAR, "*", null, 1), 
            new Expr.Unary(
                new Token(TokenType.PLUS, "+", null, 1), 
                new Expr.Literal(1)
            )
        );
        System.out.println("parse tree associated with (-123)*(1)");
        System.out.println(new AstPrinter().print(expr));   
    }
}
