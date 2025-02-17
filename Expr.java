import java.util.List;
abstract class Expr{
	interface Visitor<R>{
		R visitBinaryExpr(Binary expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitUnaryExpr(Unary expr);
		R visitVariableExpr(Variable expr);
	}
	abstract <R> R accept(Visitor<R> visitor);
	static class Binary extends Expr{
		Binary(Expr l, Token op, Expr r){
			this.l = l;
			this.op = op;
			this.r = r;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitBinaryExpr(this);
		}
		final Expr l;
		final Token op;
		final Expr r;
	}
	static class Grouping extends Expr{
		Grouping(Expr expr){
			this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitGroupingExpr(this);
		}
		final Expr expr;
	}
	static class Literal extends Expr{
		Literal(Object val){
			this.val = val;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitLiteralExpr(this);
		}
		final Object val;
	}
	static class Unary extends Expr{
		Unary(Token op, Expr r){
			this.op = op;
			this.r = r;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitUnaryExpr(this);
		}
		final Token op;
		final Expr r;
	}
	static class Variable extends Expr{
		Variable(Token name){
			this.name = name;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitVariableExpr(this);
		}
		final Token name;
	}
}