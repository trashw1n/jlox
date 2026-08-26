import java.util.Map;
import java.util.List;

public class LoxClass implements LoxCallable{
    final String name;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods){
        this.name = name;
        this.methods = methods;
    }

    LoxFunction findMethod(String name){
        if(methods.containsKey(name)) return methods.get(name);
        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        LoxInstance inst = new LoxInstance(this);
        return inst;
    }
    @Override
    public int arity(){
        return 0;
    }
    @Override 
    public String toString(){
        return "<class " + name + ">";
    }
}
