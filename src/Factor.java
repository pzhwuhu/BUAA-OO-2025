import java.math.BigInteger;

public class Factor {
    private BigInteger index = BigInteger.ONE;

    public void setIndex(BigInteger index) {
        this.index = index;
    }

    public BigInteger getIndex() {
        return index;
    }

    public Poly toPoly() {
        return null;
    }
}
