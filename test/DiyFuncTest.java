import java.util.ArrayList;
import static org.junit.Assert.*;

public class DiyFuncTest {
    private static final String fn = "-3*f{n-1}((x-1),y^2)+4*f{n-2}(x^2,(y-1))";
    @org.junit.Test
    public void addFunc() {
        ArrayList<String> define =  new ArrayList<>();
        define.add("f{0}(x,y)=x+y");
        define.add("f{1}(x,y)=x^2-y^2");
        define.add("f{n}(x,y)=-3*f{n-1}((x-1),y^2)+4*f{n-2}(x^2,(y-1))");
        DiyFunc.addFunc(define);
    }

    @org.junit.Test
    public void deployFunc() {
        ArrayList<String> define =  new ArrayList<>();
        define.add("f{0}(y,x)=x^2");
        define.add("f{1}(y,x)=y-1");
        define.add("f{n}(y,x)=-3*f{n-1}(y^3)-4*f{n-2}((y-1))");
        DiyFunc.addFunc(define);
        ArrayList<Factor> actualParam = new ArrayList<>();
        actualParam.add(new NumFactor("0"));
        //actualParam.add(new NumFactor("3"));
        System.out.println(DiyFunc.deployFunc(2, "f", actualParam));
    }

    @org.junit.Test
    public void recurrence2() {
        ArrayList<String> formParaList =  new ArrayList<>();
        formParaList.add("x");
        formParaList.add("y");

        ArrayList<String> define =  new ArrayList<>();
        define.add("f{0}(x,y)=x+y");
        define.add("f{1}(x,y)=x^2-y^2");
        define.add("f{n}(x,y)=-3*f{n-1}((x-1),y^2)+4*f{n-2}(x^2,(y-1))");
    }

    @org.junit.Test
    public void getParam() {
        String fn = "-3*f{n-1}((x-1),y^2)+4*f{n-2}(x^2,(y-1))";
        ArrayList<String> fnParaList = DiyFunc.getParam(fn);
        for (String fnPara : fnParaList) {
            System.out.println(fnPara);
        }
    }
}