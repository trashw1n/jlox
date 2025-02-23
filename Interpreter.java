import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment env = new Environment();
    public void interpret(List<Stmt> statements){
        try{
            for(Stmt stmt: statements) exec(stmt);
        } catch(RuntimeError e){
            Lox.runtimeError(e);
        }
    }
    //implementing Expr visitor
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.val;
    }
    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object l = eval(expr.l);
        //short-circuiting 
        if(expr.op.type == TokenType.OR){
            if(isTruthy(l)) return l;
        } else{
            if(!isTruthy(l)) return l;
        }
        return eval(expr.r);
    }
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return eval(expr.expr);
    }
    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object r = eval(expr.r);
        switch(expr.op.type){
            case BANG:
                return isTruthy(r);
            case MINUS:
                checkNumberOperand(expr.op, r);
                return -(double)r;
        }
        return null;
    }
    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object l = eval(expr.l), r = eval(expr.r);
        switch(expr.op.type){
            case GREATER:
                checkNumberOperands(expr.op, l, r);
                return (double)l > (double)r;
            case GREATER_EQUAL:
                checkNumberOperands(expr.op, l, r);
                return (double)l >= (double)r;    
            case LESS:
                checkNumberOperands(expr.op, l, r);
                return (double)l < (double)r;   
            case LESS_EQUAL:
                checkNumberOperands(expr.op, l, r);
                return (double)l <= (double)r;        
            case MINUS:
                checkNumberOperands(expr.op, l, r);
                return (double)l - (double)r;
            case PLUS:
                if(l instanceof Double && r instanceof Double) return (double)l + (double)r;
                if(l instanceof String && r instanceof String) return (String)l + (String)r;
                throw new RuntimeError(expr.op, "Both operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.op, l, r);
                return (double)l / (double)r;
            case STAR:
                checkNumberOperands(expr.op, l, r);
                return (double)l * (double)r;
            case BANG_EQUAL:
                return !isEqual(l, r);
            case EQUAL_EQUAL:
                return isEqual(l, r);

        }
        return null;
    }
    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return env.get(expr.name);
    }
    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object val = eval(expr.value);
        env.assign(expr.name, val);
        return val;
    }
    //util
    private Object eval(Expr expr){
        return expr.accept(this); 
    }
    private boolean isTruthy(Object obj){
        if(obj == null) return false;
        if(obj instanceof Boolean) return (boolean)obj;
        return true;
    }
    private boolean isEqual(Object a, Object b){
        if(a == null && b == null) return true;
        if(a == null) return false;
        return a.equals(b);
    }
    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
    private void checkNumberOperands(Token op, Object l, Object r){
        if(l instanceof Double && r instanceof Double) return;
        throw new RuntimeError(op, "Operands must be numbers.");
    }
    private String stringify(Object obj){
        if(obj == null) return "nil";
        if(obj instanceof Double){
            String txt = obj.toString();
            if(txt.endsWith(".0")) txt = txt.substring(0, txt.length()-2);
            return txt; 
        }
        return obj.toString();
    }
    private void exec(Stmt stmt){
        stmt.accept(this);
    }
    void execBlock(List<Stmt> statements, Environment env){
        Environment prev = this.env;
        try{
            this.env = env;
            for(Stmt stmt: statements) exec(stmt);
        } finally{
            this.env = prev;
        }
    }
    //implementing Stmt visitor
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        eval(stmt.expr);
        return null;
    }
    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object val = eval(stmt.expr);
        System.out.println(stringify(val));
        return null;
    }
    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        Object val = null;
        if(stmt.initializer != null) val = eval(stmt.initializer);
        env.define(stmt.name.lexeme, val);
        return null;
    }
    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        execBlock(stmt.statements, new Environment(env));
        return null;
    }
    @Override 
    public Void visitIfStmt(Stmt.If stmt){
        if(isTruthy(eval(stmt.condition))) exec(stmt.thenBranch);
        else if(stmt.elseBranch != null) exec(stmt.elseBranch);
        return null;
    }
}
