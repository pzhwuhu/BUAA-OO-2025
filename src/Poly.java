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
        monos.add(new Mono(BigInteger.valueOf(sign),0));
    } //便于控制符号(前导-1)

    public void addMono(Mono m) {
        monos.add(m);
    }

    public Poly addPoly(Poly others) {
        Poly newPoly = new Poly();
        HashMap<Integer, BigInteger> map = new HashMap<>();
        for (Mono m : monos) {
            map.put(m.getIndex(), m.getRadio());
        }
        for (Mono other : others.getMonos()) {
            if (map.containsKey(other.getIndex())) {
                BigInteger newRadio = map.get(other.getIndex()).add(other.getRadio());
                map.replace(other.getIndex(), newRadio);
            }
            else {
                map.put(other.getIndex(), other.getRadio());
            }
        }
        for (Integer key : map.keySet()) {
            newPoly.addMono(new Mono(map.get(key), key));
        }
        return newPoly;
    }

    public Poly multiPoly(Poly others) {
        Poly empty = new Poly(); //空的，用来将多项式相加合并
        Poly newPoly = new Poly(); //存储结果
        for (Mono m : monos) {
            for (Mono other : others.getMonos()) {
                BigInteger radio = m.getRadio().multiply(other.getRadio());
                int index = m.getIndex() + other.getIndex();
                Mono res = new Mono(radio, index);
                newPoly.addMono(res);
            }
        }
        return empty.addPoly(newPoly);
    }

    public Poly powerPoly(int index) {
        Poly newPoly = new Poly(1);
        for (int i = 0;i < index;i++) {
            newPoly = newPoly.multiPoly(this);
        }
        return newPoly;
    }

    public ArrayList<Mono> getMonos() {
        return monos;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        for (Mono m : monos) {
            if (m.getIndex() == 0) {
                sb.append(m.getRadio().toString() + "+");
            }
            else if (m.getRadio().equals(BigInteger.ZERO)) {
                //sb.append("0+");
            }
            else if (m.getIndex() == 1) {
                if (m.getRadio().equals(BigInteger.ONE)) {
                    sb.append("x+");
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
