import java.math.BigInteger;
import java.util.ArrayList;

public class Mono {
    private BigInteger radio = BigInteger.ONE;
    private BigInteger index = BigInteger.ZERO;
    private ArrayList<TriFactor> triFactors = new ArrayList<>();

    public Mono(BigInteger radio, BigInteger index) {
        this.radio = radio;
        this.index = index;
    }

    public Mono(TriFactor triFactor) {
        this.triFactors = new ArrayList<>();
        this.triFactors.add(triFactor);
    }

    public void addTriFactors(ArrayList<TriFactor> newTriFactors) {
        this.triFactors.addAll(newTriFactors);
    }

    public void addRadio(BigInteger newRadio) {
        this.radio = this.radio.add(newRadio);
    }

    public boolean mergeAble(Mono mono) {
        if (this.getIndex().equals(mono.getIndex())) {
            return this.triFactors.equals(mono.getTriFactors());
        }
        return false;
    }

    public ArrayList<TriFactor> getTriFactors() {
        return triFactors;
    }

    public BigInteger getRadio() {
        return radio;
    }

    public BigInteger getIndex() {
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(radio.toString() + "*x^" + index.toString());
        for (TriFactor triFactor : triFactors) {
            sb.append("*" + triFactor.toString());
        }
        return sb.toString();
    }

}
