import java.math.BigInteger;

public class Mono {
    private final BigInteger radio;
    private final int index;

    public Mono(BigInteger radio, int index) {
        this.radio = radio;
        this.index = index;
    }

    public BigInteger getRadio() {
        return radio;
    }

    public int getIndex() {
        return index;
    }

}
