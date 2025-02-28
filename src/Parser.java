public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        int sign = 1;
        if (lexer.getCurrentToken().getType() == Token.Type.OP) {
            if (lexer.getCurrentToken().getValue().equals("-")) {
                sign = -1;
            }
            lexer.nextToken();
        }
        expr.addTerm(parseTerm(sign));
        while (!lexer.isEnd() && lexer.getCurrentToken().getType() == Token.Type.OP) {
            if (lexer.getCurrentToken().getValue().equals("-")) {
                lexer.nextToken();
                expr.addTerm(parseTerm(-1));
            }
            else {
                lexer.nextToken();
                expr.addTerm(parseTerm(1));
            }
        }
        //System.out.println("Expr: " + expr.toString());
        return expr;
    }

    public Term parseTerm(int sign) {
        Term term = new Term(sign);
        term.addFactor(parseFactor());
        while (!lexer.isEnd() && lexer.getCurrentToken().getType() == Token.Type.MUL) {
            lexer.nextToken();
            term.addFactor(parseFactor());
        }
        //System.out.println("Term: " + term.toString() + " Sign: " + sign);
        return term;
    }

    public Factor parseFactor() {
        Token token = lexer.getCurrentToken();
        Factor factor;
        if (token.getType() == Token.Type.NUM) {
            factor = new NumFactor(token.getValue());
            lexer.nextToken();
        }
        else if (token.getType() == Token.Type.VAR) {
            factor = new VarFactor(token.getValue());
            lexer.nextToken();
        }
        else {
            lexer.nextToken();
            factor = new ExprFactor(parseExpr());
            lexer.nextToken();
        } //token.getType() == Token.Type.LPAREN

        if (!lexer.isEnd() && lexer.getCurrentToken().getType() == Token.Type.POWER) {
            lexer.nextToken();
            int index = Integer.parseInt(lexer.getCurrentToken().getValue());
            factor.setIndex(index);
            lexer.nextToken();
        }

        //System.out.println(factor.getClass().getName() + ": " + factor.toString() + " Index: " + factor.getIndex());
        return factor;
    }

}
