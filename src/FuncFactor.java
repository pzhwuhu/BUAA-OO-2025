public class FuncFactor extends Factor {
    private String func;
    private Expr expr;

    public FuncFactor(String func, Expr expr) {
        this.func = func;
        this.expr = expr;
    }

    public Poly toPoly() {
        Poly poly = expr.toPoly();
        poly = poly.powerPoly(super.getIndex());
        return poly;
    }

    @Override
    public String toString() {
        return func;
    }
}
