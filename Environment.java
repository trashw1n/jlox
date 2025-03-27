import java.util.Map;
import java.util.HashMap;

public class Environment {
    public final Environment enclosing; 
    private final Map<String, Object> values = new HashMap<>();
    Environment(){
        enclosing = null;
    }
    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }
    void define(String name, Object val){
        values.put(name, val);
    }
    Object get(Token name){
        if(values.containsKey(name.lexeme)) return values.get(name.lexeme);
        if(enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
    Object getAt(int dist, String name){
        return ancestor(dist).values.get(name);
    }
    void assign(Token name, Object val){
        //only allow assignment to defined variables
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme, val);
            return;
        }
        if(enclosing != null){
            enclosing.assign(name, val);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
    void assignAt(int dist, Token name, Object val){
        ancestor(dist).values.put(name.lexeme, val);
    }
    private Environment ancestor(int dist){
        Environment env = this;
        for(int i = 0; i < dist; i++) env = env.enclosing;
        return env;
    }
}
