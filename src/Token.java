public class Token {
    public enum Type {
        OP, MUL, POWER, NUM, VAR, LPAREN, RPAREN, TRI, FUNC, COMMA
    }

    private final Type type;
    private final String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() { return type; }

    public String getValue() { return value; }
}
