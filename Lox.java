import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean errorOccurred = false;
    public static void main(String[] args) throws IOException{
        if(args.length > 1){
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if(args.length == 1) runFile(args[0]);
        else runPrompt();
    }

    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(errorOccurred) System.exit(65);
    }

    private static void runPrompt() throws IOException{
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inp);
        for(;;){
            System.out.println("> ");
            String ln = reader.readLine();
            if(ln == null) break;
            run(ln);
            errorOccurred = false;
        }
    }

    private static void run(String src){
        Scanner sc = new Scanner(src);
        List<Token> tokens = sc.scanTokens();
        for(Token token: tokens) System.out.println(token + " ");
    }
    
    static void error(int ln, String msg){
        report(ln, "", msg);
    }

    private static void report(int ln, String where, String msg){
        System.err.println(
            "[line " + ln + "] Error" + where + ": " + msg
        );
        errorOccurred = true;
    }
}