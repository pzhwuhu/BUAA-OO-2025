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

    public Mono(BigInteger radio, BigInteger index, ArrayList<TriFactor> triFactors) {
        this.radio = radio;
        this.index = index;
        this.triFactors = triFactors;
    }

    public ArrayList<TriFactor> mergeTriFactors(ArrayList<TriFactor> newTriFactors) {
        ArrayList<TriFactor> mergedTriFactors = new ArrayList<>();
        for (TriFactor triFactor : triFactors) {
            mergedTriFactors.add(triFactor.deepClone());
        }
        for (TriFactor newTriFactor : newTriFactors) {
            int flag = 0;
            for (TriFactor triFactor : mergedTriFactors) {
                String newTri = newTriFactor.toString();
                String oldTri = triFactor.toString();
                if (newTri.equals(oldTri)) {
                    triFactor.setIndex(triFactor.getIndex().add(newTriFactor.getIndex()));
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                mergedTriFactors.add(newTriFactor.deepClone());
            }
        }
        return mergedTriFactors;
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
        if (radio.equals(BigInteger.ZERO)) {
            sb.append("0");
        }
        else {
            if (index.equals(BigInteger.ZERO)) {
                if (radio.equals(BigInteger.ONE)) {
                    if (triFactors.isEmpty()) {
                        sb.append("1");
                    }
                }
                else if (radio.equals(BigInteger.valueOf(-1))) {
                    sb.append("-1");
                }
                else {
                    sb.append(radio);
                }
            }
            else if (index.equals(BigInteger.ONE)) {
                if (radio.equals(BigInteger.valueOf(-1))) {
                    sb.append("-x");
                }
                else if (radio.equals(BigInteger.ONE)) {
                    sb.append("x");
                }
                else {
                    sb.append(radio + "*x");
                }
            }
            else {
                if (radio.equals(BigInteger.valueOf(-1))) {
                    sb.append("-x^" + index);
                }
                else if (radio.equals(BigInteger.ONE)) {
                    sb.append("x^" + index);
                }
                else {
                    sb.append(radio + "*x^" + index);
                }
            }
            for (TriFactor triFactor : triFactors) {
                if (sb.length() > 0) {
                    sb.append("*" + triFactor.toString());
                }
                else {
                    sb.append(triFactor.toString());
                }
            }
        }
        return sb.toString();
    }

}
