import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private enum FunctionType{ 
        NONE, 
        FUNCTION,
        CONSTRUCTOR,
        METHOD
    }
    private enum ClassType{
        NONE,
        CLASS
    }
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private Boolean withinLoop = false;
    private FunctionType currFn = FunctionType.NONE; 
    private ClassType currCl = ClassType.NONE;
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
    public Void visitClassStmt(Stmt.Class stmt){
        ClassType enclosingCl = currCl;
        currCl = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);
        if(stmt.superclass != null && stmt.superclass.name.lexeme.equals(stmt.name.lexeme)){
            Lox.error(stmt.superclass.name, "A class cannot inherit from itself.");
        }
        //storing superclass as Expr.Variable allows us to resolve it here.
        if(stmt.superclass != null) resolve(stmt.superclass);
        //resolve 'this' to an implicit block around method definitions
        beginScope();
        scopes.peek().put("this", true);
        for(Stmt.Function method : stmt.methods){
            FunctionType type = FunctionType.METHOD;
            if(method.name.lexeme.equals("init")) type = FunctionType.CONSTRUCTOR; 
            resolveFunction(method, type);
        }
        endScope();
        currCl = enclosingCl;
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
        resolveFunction(stmt, FunctionType.FUNCTION);
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
    public Void visitBreakStmt(Stmt.Break stmt){
        if(!withinLoop){
            Lox.error(stmt.keyword, "Cannot use 'break' outside loops.");
        }
        return null;
    }
    @Override
    public Void visitContinueStmt(Stmt.Continue stmt){
        if(!withinLoop){
            Lox.error(stmt.keyword, "Cannot use 'continue' outside loops.");
        }
        return null;
    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if(currFn == FunctionType.NONE){
            Lox.error(stmt.keyword, "Cannot return from top-level code.");
        }
        if(stmt.value != null && currFn == FunctionType.CONSTRUCTOR){
            Lox.error(stmt.keyword, "Cannot return values from a instance initializer.");
        }
        if(stmt.value != null) resolve(stmt.value);
        return null;
    }
    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        Boolean enclosingWithinLoop = withinLoop;
        withinLoop = true;
        resolve(stmt.condition);
        resolve(stmt.body);
        withinLoop = enclosingWithinLoop;
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
    public Void visitGetExpr(Expr.Get expr){
        //resolve just the target object/LHS of property getters since actual properties are looked up at runtime.
        resolve(expr.object); 
        return null;
    }
    @Override
    public Void visitSetExpr(Expr.Set expr){
        //resolve just the 'getter' part and the r-value part of property setting/assigment.
        resolve(expr.value);
        resolve(expr.object);
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
    public Void visitThisExpr(Expr.This expr){
        if(currCl == ClassType.NONE){
            Lox.error(expr.keyword, "Cannot use 'this' outside classes.");
            return null;
        }
        resolveLocal(expr, expr.keyword);
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
    private void resolveFunction(Stmt.Function fn, FunctionType type){
        FunctionType enclosingFn = currFn;
        currFn = type;
        beginScope();
        for(Token param: fn.params){
            declare(param);
            define(param);
        }
        resolve(fn.body);
        endScope();
        currFn = enclosingFn;
    }
    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }
    private void endScope(){
        scopes.pop();
    }
    private void declare(Token name){
        if(scopes.isEmpty()) return;
        if(scopes.peek().containsKey(name.lexeme)){
            Lox.error(name, "Cannot redeclare multiple variables with same name in the scope.");
        }
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