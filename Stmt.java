import java.util.List;
abstract class Stmt{
	interface Visitor<R>{
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
	}
	abstract <R> R accept(Visitor<R> visitor);
	static class Expression extends Stmt{
		Expression(Expr expr){
			this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitExpressionStmt(this);
		}
		final Expr expr;
	}
	static class Print extends Stmt{
		Print(Expr expr){
			this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitPrintStmt(this);
		}
		final Expr expr;
	}
	static class Var extends Stmt{
		Var(Token name, Expr initializer){
			this.name = name;
			this.initializer = initializer;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitVarStmt(this);
		}
		final Token name;
		final Expr initializer;
	}
}