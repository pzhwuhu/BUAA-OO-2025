import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Poly {
    private ArrayList<Mono> monos;

    public Poly() {
        monos = new ArrayList<>();
    }

    public Poly(int sign) {
        monos = new ArrayList<>();
        monos.add(new Mono(BigInteger.valueOf(sign),BigInteger.ZERO));
    } //便于控制符号(前导-1)

    public void addMono(Mono m) {
        monos.add(m);
    }

    public ArrayList<Mono> getMonos() {
        return monos;
    }

    public Poly addPoly(Poly others) {
        HashMap<BigInteger, BigInteger> map = new HashMap<>();
        for (Mono m : monos) {
            map.put(m.getIndex(), m.getRadio());
        }
        for (Mono other : others.getMonos()) {
            if (map.containsKey(other.getIndex())) {
                int flag = 0;
                for (Mono m : monos) {
                    if (other.mergeAble(m)) {
                        m.addRadio(other.getRadio());
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    monos.add(other);
                }
            }
            else {
                monos.add(other);
            }
        }
        return this;
    }

    public Poly multiPoly(Poly others) {
        Poly empty = new Poly(); //空的，用来将多项式相加合并
        Poly newPoly = new Poly(); //存储结果
        for (Mono m : monos) {
            for (Mono other : others.getMonos()) {
                BigInteger radio = m.getRadio().multiply(other.getRadio());
                BigInteger index = m.getIndex().add(other.getIndex());
                Mono res = new Mono(radio, index);
                res.addTriFactors(other.getTriFactors());
                res.addTriFactors(m.getTriFactors());
                newPoly.addMono(res);
            }
        }
        return empty.addPoly(newPoly);
    }

    public Poly powerPoly(BigInteger index) {
        Poly newPoly = new Poly(1);
        for (BigInteger i = BigInteger.ZERO; i.compareTo(index) < 0; i = i.add(BigInteger.ONE)) {
            newPoly = newPoly.multiPoly(this);
        }
        return newPoly;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        for (Mono m : monos) {
            if (m.getTriFactors().isEmpty()) {
                if (m.getIndex().equals(BigInteger.ZERO)) {
                    sb.append(m.getRadio().toString() + "+");
                }
                else if (m.getRadio().equals(BigInteger.ZERO)) {
                    //sb.append("0+");
                }
                else if (m.getIndex().equals(BigInteger.ONE)) {
                    if (m.getRadio().equals(BigInteger.ONE)) {
                        sb.append("x+");
                    }
                    else if (m.getRadio().equals(BigInteger.valueOf(-1))) {
                        sb.append("-x" + "+");
                    }
                    else {
                        sb.append(m.getRadio().toString() + "*x" + "+");
                    }
                }
                else if (m.getRadio().equals(BigInteger.ONE)) {
                    sb.append("x^" + String.valueOf(m.getIndex()) + "+");
                }
                else if (m.getRadio().equals(BigInteger.valueOf(-1))) {
                    sb.append("-x^" + String.valueOf(m.getIndex()) + "+");
                }
                else {
                    sb.append(m.getRadio().toString() + "*x^" + String.valueOf(m.getIndex()) + "+");
                }
            }
            else {
                sb.append(m.toString() + "+");
            }
        }
        if (sb.length() > 0) {
            String str = sb.substring(0, sb.length() - 1);
            str = str.replaceAll("\\+-", "-");
            str = str.replaceAll("-\\+", "-");
            System.out.println(str);
        }
        else {
            System.out.println("0");
        }
    }

}
