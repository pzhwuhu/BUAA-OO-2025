import java.util.ArrayList;

public class Lexer {
    private int index = 0;
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int pos = 0;
    private String expr = null;

    public Lexer(String input) {
        this.expr = preProcess(input);
        //System.out.println(expr);
        lexerTokens();
    }

    private String preProcess(String input) {
        String output = input.replaceAll("[ \t]", "");
        for (int i = 0; i < 3; i++) {
            output = output.replaceAll("\\+\\+", "+");
            output = output.replaceAll("--", "+");
            output = output.replaceAll("\\+-", "-");
            output = output.replaceAll("-\\+", "-");
        }
        return output;
    }

    private void lexerTokens() {
        while (pos < expr.length()) {
            if (expr.charAt(pos) == '(') {
                tokens.add(new Token(Token.Type.LPAREN, "("));
                pos++;
            }
            else if (expr.charAt(pos) == ')') {
                tokens.add(new Token(Token.Type.RPAREN, ")"));
                pos++;
            }
            else if (expr.charAt(pos) == '+') {
                if (pos > 1 && (expr.charAt(pos - 1) == '^' || expr.charAt(pos - 1) == '*')) {
                    pos++;
                    parseNumber(1);
                }
                else {
                    tokens.add(new Token(Token.Type.OP, "+"));
                    pos++;
                }
            }
            else if (expr.charAt(pos) == '-') {
                if (pos > 1 && expr.charAt(pos - 1) == '*') {
                    pos++;
                    parseNumber(-1);
                }
                else {
                    tokens.add(new Token(Token.Type.OP, "-"));
                    pos++;
                }
            }
            else if (expr.charAt(pos) == '*') {
                tokens.add(new Token(Token.Type.MUL, "*"));
                pos++;
            }
            else if (expr.charAt(pos) == '^') {
                tokens.add(new Token(Token.Type.POWER, "^"));
                pos++;
            }
            else if (expr.charAt(pos) == 'x') {
                tokens.add(new Token(Token.Type.VAR, "x"));
                pos++;
            }
            else {
                parseNumber(1);
            }
        }
    }

    private void parseNumber(int sign) {

        char now = expr.charAt(pos);
        StringBuilder sb = new StringBuilder();
        if (sign == -1) {
            sb.append('-');
        }
        while (now >= '0' && now <= '9') {
            sb.append(now);
            pos++;
            if (pos >= expr.length()) {
                break;
            }
            now = expr.charAt(pos);
        }
        tokens.add(new Token(Token.Type.NUM, sb.toString()));
    }

    public Token getCurrentToken() {
        return tokens.get(index);
    }

    public void nextToken() {
        index++;
    }

    public boolean isEnd() {
        return index >= tokens.size();
    }
}
