outputDir = "C:/jlox"

ExprBase = "Expr"
ExprPath = outputDir + "/" + ExprBase + ".java"
ExprTypes = {
    "Assign": "Token name, Expr value",
    "Binary": "Expr l, Token op, Expr r",
    "Grouping": "Expr expr",
    "Literal": "Object val",
    "Logical": "Expr l, Token op, Expr r",
    "Unary": "Token op, Expr r",
    "Variable": "Token name",
    "Call": "Expr callee, Token paren, List<Expr> arguments"
}

StmtBase = "Stmt"
StmtPath = outputDir + "/" + StmtBase + ".java"
StmtTypes = {
    "Block": "List<Stmt> statements",
    "Expression": "Expr expr",
    "Function": "Token name, List<Token> params, List<Stmt> body",
    "If": "Expr condition, Stmt thenBranch, Stmt elseBranch",
    "Print": "Expr expr",
    "Return": "Token keyword, Expr value",
    "Var": "Token name, Expr initializer",
    "While": "Expr condition, Stmt body"
}

def defineClass(base, path, types):
    with open(path, "w") as out:
        out.write("import java.util.List;\n")
        out.write("abstract class " + base + "{\n")

        #define visitor class
        out.write("\tinterface Visitor<R>{\n")
        for typeName, fields in types.items():
            out.write("\t\tR visit" + typeName + base + "(" + typeName + " " + base.lower() + ");\n")
        out.write("\t}\n")
        #abstract accept definition
        out.write("\tabstract <R> R accept(Visitor<R> visitor);\n")

        #types 
        for className, fields in types.items():
            out.write("\tstatic class " + className + " extends " + base + "{\n")
            #constructor
            out.write("\t\t" + className + "(" + fields + "){\n")
            fieldsList = fields.split(", ")
            for field in fieldsList:
                name = field.split()[1]
                out.write("\t\t\tthis." + name + " = " + name + ";\n")
            out.write("\t\t}\n")
            #accept method
            out.write("\t\t@Override\n")
            out.write("\t\t<R> R accept(Visitor<R> visitor){\n")
            out.write("\t\t\treturn visitor.visit" + className + base + "(this);\n")
            out.write("\t\t}\n")
            #data fields
            for field in fieldsList:
                out.write("\t\tfinal " + field + ";\n")
            out.write("\t}\n")
        out.write("}")

defineClass(ExprBase, ExprPath, ExprTypes)   
defineClass(StmtBase, StmtPath, StmtTypes)