import java.math.BigInteger;
import java.util.ArrayList;

public class TriFactor extends Factor {
    private Factor subFactor;
    private String type;

    public TriFactor(Factor subFactor, String type) {
        this.subFactor = subFactor;
        this.type = type;
    }

    public TriFactor deepClone() {
        TriFactor triFactor = new TriFactor(subFactor, type);
        triFactor.setIndex(super.getIndex());
        return triFactor;
    }

    public Poly derive() {
        Poly derived;
        Mono mono;
        ArrayList<TriFactor> newTriFactors = new ArrayList<>();
        if (type.equals("sin")) {
            if (super.getIndex().equals(BigInteger.ZERO)) {
                ;
            }
            else if (super.getIndex().equals(BigInteger.ONE)) {
                TriFactor triFactor2 = new TriFactor(subFactor, "cos");
                newTriFactors.add(triFactor2);
            }
            else {
                TriFactor triFactor1 = new TriFactor(subFactor, "sin");
                triFactor1.setIndex(super.getIndex().add(new BigInteger("-1")));
                newTriFactors.add(triFactor1);
                TriFactor triFactor2 = new TriFactor(subFactor, "cos");
                newTriFactors.add(triFactor2);
            }
            mono = new Mono(super.getIndex(), BigInteger.ZERO, newTriFactors);
        }
        else {
            if (super.getIndex().equals(BigInteger.ZERO)) {
                ;
            }
            else if (super.getIndex().equals(BigInteger.ONE)) {
                TriFactor triFactor2 = new TriFactor(subFactor, "sin");
                newTriFactors.add(triFactor2);
            }
            else {
                TriFactor triFactor1 = new TriFactor(subFactor, "cos");
                triFactor1.setIndex(super.getIndex().add(new BigInteger("-1")));
                newTriFactors.add(triFactor1);
                TriFactor triFactor2 = new TriFactor(subFactor, "sin");
                newTriFactors.add(triFactor2);
            }
            mono = new Mono(super.getIndex().negate(), BigInteger.ZERO, newTriFactors);
        }
        Poly part1 = new Poly(mono);
        Poly part2 = subFactor.toPoly().derive();
        derived = part1.multiPoly(part2);
        return derived;
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

    public boolean equals(TriFactor triFactor) {
        return this.toString().equals(triFactor.toString());
    }
}
