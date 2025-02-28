import java.util.ArrayList;

public class Expr {
    private ArrayList<Term> terms = new ArrayList<>();

    public void addTerm(Term t) {
        terms.add(t);
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        for (Term t : terms) {
            poly = poly.addPoly(t.toPoly());
        }
        //System.out.println("Expr toPoly finished, size:" + poly.getMonos().size());
        return poly;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Term t : terms) {
            sb.append(t.toString());
            sb.append("+");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
