import java.math.BigInteger;
import java.util.ArrayList;

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
        else if (token.getType() == Token.Type.TRI) {
            String tri = token.getValue();
            lexer.moveToken(2);
            Factor subFactor = parseFactor();
            factor = new TriFactor(subFactor, tri);
            lexer.nextToken();
        }
        else if (token.getType() == Token.Type.FUNC) {
            if(token.getValue().equals("f")) {
                factor = parseRecurFuncFactor();
            }
            else {
                factor = parseNormalFuncFactor();
            }
        }
        else if(token.getType() == Token.Type.DERIVE) {
            lexer.moveToken(2);
            Expr expr = parseExpr();
            factor = new DeriveFactor(expr);
            lexer.nextToken();
        }
        else {
            lexer.nextToken();
            Expr expr = parseExpr();
            factor = new ExprFactor(expr);
            lexer.nextToken();
        } //token.getType() == Token.Type.LPAREN

        if (!lexer.isEnd() && lexer.getCurrentToken().getType() == Token.Type.POWER) {
            lexer.nextToken();
            String index = lexer.getCurrentToken().getValue();
            factor.setIndex(new BigInteger(index));
            lexer.nextToken();
        }
        return factor;
    }

    public FuncFactor parseRecurFuncFactor() {
        final String funcName = lexer.getCurrentToken().getValue();
        ArrayList<Factor> actualParams = new ArrayList<>();
        lexer.moveToken(2);
        final int n = Integer.parseInt(lexer.getCurrentToken().getValue());
        lexer.moveToken(3);
        actualParams.add(parseFactor());
        while (lexer.getCurrentToken().getType() == Token.Type.COMMA) {
            lexer.nextToken();
            actualParams.add(parseFactor());
        }
        lexer.nextToken();//退出括号

        String func = DiyFunc.deployFunc(n, funcName, actualParams);
        //System.out.println(func);
        Lexer newLexer = new Lexer(func);
        Parser newParser = new Parser(newLexer);
        Expr expr = newParser.parseExpr();

        return new FuncFactor(func, expr);
    }

    public FuncFactor parseNormalFuncFactor() {
        final String funcName = lexer.getCurrentToken().getValue();
        ArrayList<Factor> actualParams = new ArrayList<>();
        lexer.moveToken(2);
        actualParams.add(parseFactor());
        while (lexer.getCurrentToken().getType() == Token.Type.COMMA) {
            lexer.nextToken();
            actualParams.add(parseFactor());
        }
        lexer.nextToken();//退出括号

        String func = NormalFunc.deploy(funcName, actualParams);
        //System.out.println(func);
        Lexer newLexer = new Lexer(func);
        Parser newParser = new Parser(newLexer);
        Expr expr = newParser.parseExpr();

        return new FuncFactor(func, expr);
    }
}
