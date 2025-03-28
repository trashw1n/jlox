import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }
    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }
    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if(stmt.initializer != null) resolve(stmt.initializer);
        define(stmt.name);
        return null;
    }
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt);
        return null;
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        resolve(stmt.expr);
        return null;
    }
    @Override
    public Void visitIfStmt(Stmt.If stmt){
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }
    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        resolve(stmt.expr);
        return null;
    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if(stmt.value != null) resolve(stmt.value);
        return null;
    }
    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }
    @Override
    public Void visitVariableExpr(Expr.Variable expr){
        if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE){
            Lox.error(expr.name, "Can't read local variable in its own initializer");
        } 
        resolveLocal(expr, expr.name);
        return null;
    }
    @Override
    public Void visitAssignExpr(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;    
    }
    @Override
    public Void visitBinaryExpr(Expr.Binary expr){
        resolve(expr.l);
        resolve(expr.r);
        return null;
    }
    @Override
    public Void visitCallExpr(Expr.Call expr){
        resolve(expr.callee);
        for(Expr arg: expr.arguments) resolve(arg);
        return null;
    }
    @Override
    public Void visitGroupingExpr(Expr.Grouping expr){
        resolve(expr.expr);
        return null;
    }
    @Override 
    public Void visitLiteralExpr(Expr.Literal expr){
        return null;
    }
    @Override
    public Void visitLogicalExpr(Expr.Logical expr){
        resolve(expr.l);
        resolve(expr.r);
        return null;
    }
    @Override 
    public Void visitUnaryExpr(Expr.Unary expr){
        resolve(expr.r);
        return null;
    }
    void resolve(List<Stmt> statements){
        for(Stmt stmt: statements){
            resolve(stmt);
        }
    }
    private void resolve(Stmt stmt){
        stmt.accept(this);  
    }
    private void resolve(Expr expr){
        expr.accept(this);
    }
    private void resolveFunction(Stmt.Function fn){
        beginScope();
        for(Token param: fn.params){
            declare(param);
            define(param);
        }
        resolve(fn.body);
        endScope();
    }
    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }
    private void endScope(){
        scopes.pop();
    }
    private void declare(Token name){
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, false);
    }
    private void define(Token name){
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }
    private void resolveLocal(Expr expr, Token name){
        for(int i = scopes.size()-1; i >= 0; i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size()-1-i);
                return;
            }
        }
    }
}