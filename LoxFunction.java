import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private Boolean isInitializer;
    LoxFunction(Stmt.Function declaration, Environment closure, Boolean isInitializer){
        this.isInitializer = isInitializer;
        this.closure = closure;
        this.declaration = declaration;
    }

    LoxFunction bind(LoxInstance inst){
        //fitting in a new environment containing 'this' one step below closure.
        Environment env = new Environment(closure);
        env.define("this", inst);
        return new LoxFunction(declaration, env, isInitializer);
    }

    @Override
    public int arity(){
        return declaration.params.size();
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        //consult closing environment first and not straightaway global environment.
        Environment env = new Environment(closure);
        for(int i = 0; i < declaration.params.size(); i++){
            env.define(declaration.params.get(i).lexeme, arguments.get(i));
        }
        try{
            interpreter.execBlock(declaration.body, env);
        } catch(Return returnVal){
            //if some LoxFunction call returns some value deep within the call stack
            if(isInitializer) return closure.getAt(0, "this"); 
            return returnVal.value;
        }
        if(isInitializer) return closure.getAt(0, "this");
        return null;
    }
    @Override
    public String toString(){
        return "<fn " + declaration.name.lexeme + ">";  
    }
}
