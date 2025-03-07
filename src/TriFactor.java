import java.math.BigInteger;

public class TriFactor extends Factor {
    private Factor subFactor;
    private String type;

    public TriFactor(Factor subFactor, String type) {
        this.subFactor = subFactor;
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (super.getIndex().equals(BigInteger.ZERO)) {
            sb.append("1");
        }
        else {
            sb.append(type.toString() + "(" + subFactor.toString() + ")");
            if (!super.getIndex().equals(BigInteger.ONE)) {
                sb.append("^" + super.getIndex());
            }
        }
        return sb.toString();
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(new Mono(this));
        return poly;
    }
}
