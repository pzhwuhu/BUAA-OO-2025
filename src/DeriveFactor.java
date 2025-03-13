public class DeriveFactor extends Factor {
    private Expr expr;
    private Poly poly;

    public DeriveFactor(Expr expr) {
        this.expr = expr;
        this.poly = expr.toPoly().derive();
    }

    @Override
    public Poly toPoly() {
        return poly;
    }

    @Override
    public String toString() {
        return poly.print();
    }
}
