import java.util.ArrayList;

public class Term {
    private final ArrayList<Factor> factors;
    private int sign = 1;

    public Term(int sign) {
        this.sign = sign;
        this.factors = new ArrayList<>();
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public Poly toPoly() {
        Poly poly = new Poly(sign);
        for (Factor factor : factors) {
            poly = poly.multiPoly(factor.toPoly());
        }
        //System.out.println("Term toPoly finished, size:" + poly.getMonos().size());
        return poly;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Factor factor : factors) {
            if (factor instanceof ExprFactor) {
                str.append("(" + factor.toString() + ")");
            }
            else {
                str.append(factor.toString());
            }
            str.append("*");
        }
        return str.substring(0, str.length() - 1);
    }
}
