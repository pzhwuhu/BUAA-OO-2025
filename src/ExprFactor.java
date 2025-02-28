public class ExprFactor extends Factor {
    private Expr expr;

    public ExprFactor(Expr expr) {
        this.expr = expr;
    }

    @Override
    public Poly toPoly() {
        Poly poly = expr.toPoly();
        poly = poly.powerPoly(super.getIndex());
        //System.out.println("ExprFactor toPoly step 2 finished, size:" + poly.getMonos().size());
        return poly;
    }

    @Override
    public String toString() {
        return expr.toString();
    }
}
