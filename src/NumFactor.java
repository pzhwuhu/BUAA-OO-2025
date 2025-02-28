import java.math.BigInteger;

public class NumFactor extends Factor {
    private BigInteger num;

    public NumFactor(String num) {
        this.num = BigInteger.valueOf(Long.parseLong(num));
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(new Mono(num, 0));
        //System.out.println("NumFactor toPoly finished, size:" + poly.getMonos().size());
        return poly;
    }

    @Override
    public String toString() {
        return num.toString();
    }
}
