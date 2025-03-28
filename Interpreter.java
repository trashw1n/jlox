import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final Map<Expr, Integer> locals = new HashMap<>();
    final Environment globals = new Environment();
    private Environment env = globals;
    Interpreter(){
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity(){
                return 0;
            }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                return (double)System.currentTimeMillis()/1000.0;
            }
            @Override
            public String toString(){
                return "<native function>";
            }
        });
    }
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
        //return env.get(expr.name);
        return lookupVariable(expr.name, expr);
    }
    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object val = eval(expr.value);
        // env.assign(expr.name, val);
        Integer dist = locals.get(expr);
        if(dist != null) env.assignAt(dist, expr.name, val);
        else globals.assign(expr.name, val);
        return val;
    }
    @Override
    public Object visitCallExpr(Expr.Call expr){
        Object callee = eval(expr.callee);
        List<Object> args = new ArrayList<>();
        for(Expr arg: expr.arguments) args.add(eval(arg));
        if(!(callee instanceof LoxCallable)){
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }
        LoxCallable fn = (LoxCallable)callee;
        if(args.size() != fn.arity()){
            throw new RuntimeError(expr.paren, "Expected " + fn.arity() + " arguments but got " +
                args.size() + ".");
        }
        return fn.call(this, args);
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
    private Object lookupVariable(Token name, Expr expr){
        Integer dist = locals.get(expr);
        if(dist != null) return env.getAt(dist, name.lexeme);
        else return globals.get(name);
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
    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        while(isTruthy(eval(stmt.condition))) exec(stmt.body);
        return null;
    }
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        LoxFunction fn = new LoxFunction(stmt, env);
        env.define(stmt.name.lexeme, fn);
        return null;
    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        Object val = null;
        if(stmt.value != null) val = eval(stmt.value);
        //using this as a control flow construct.
        throw new Return(val);
    }
    void resolve(Expr expr, int depth){
        locals.put(expr, depth);
    }
}
