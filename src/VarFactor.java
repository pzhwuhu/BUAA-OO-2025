import java.math.BigInteger;

public class VarFactor extends Factor {
    private String variable;

    public VarFactor(String variable) {
        this.variable = variable;
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(new Mono(BigInteger.ONE, super.getIndex()));
        //System.out.println("VarFactor toPoly finished, size:" + poly.getMonos().size());
        return poly;
    }

    @Override
    public String toString() {
        if (super.getIndex().equals(BigInteger.ZERO)) {
            return "1";
        }
        else if (super.getIndex().equals(BigInteger.ONE)) {
            return variable;
        }
        else {
            return variable + "^" + super.getIndex().toString();
        }
    }
}
