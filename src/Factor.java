import java.math.BigInteger;

public class Factor {
    private BigInteger index = BigInteger.ONE;

    public void setIndex(String index) {
        this.index = new BigInteger(index);
    }

    public BigInteger getIndex() {
        return index;
    }

    public Poly toPoly() {
        return null;
    }
}
