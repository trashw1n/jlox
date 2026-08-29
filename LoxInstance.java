import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass cl;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass cl){
        this.cl = cl;
    }

    Object get(Token name){
        if(fields.containsKey(name.lexeme)) return fields.get(name.lexeme);
        LoxFunction method = cl.findMethod(name.lexeme);
        if(method != null) return method.bind(this);
        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value){
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString(){
        return "<instance " + cl.name + ">";
    }
}
