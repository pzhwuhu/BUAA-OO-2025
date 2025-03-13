import java.util.ArrayList;
import java.util.HashMap;

public class NormalFunc {
    private static final HashMap<String, String> functions = new HashMap<>();//由函数名得到表达式
    private static final HashMap<String, String> formParas = new HashMap<>();//由函数名得形参

    public static void addFunc(ArrayList<String> define) {
        for (String g : define) {
            final String funcName = String.valueOf(g.charAt(0));
            int index = 2;
            StringBuilder formPara = new StringBuilder();
            formPara.append(g.charAt(index++));
            if (g.charAt(index) == ',') {
                index++;
                formPara.append(g.charAt(index++));
            } //g.charAt(index) == ')'
            index += 2;
            String func = g.substring(index);
            functions.put(funcName, func);
            formParas.put(funcName, formPara.toString());
        }
    }

    public static String deploy(String funcName, ArrayList<Factor> actualParas) {
        String func = functions.get(funcName);
        String formPara1 = String.valueOf(formParas.get(funcName).charAt(0));
        String actualPara1 = actualParas.get(0).toString();
        String realFunc = func.replace(formPara1, "$0");
        if (actualParas.size() == 2) {
            String formPara2 = String.valueOf(formParas.get(funcName).charAt(1));
            String actualPara2 = actualParas.get(1).toString();
            realFunc = realFunc.replace(formPara2, actualPara2);
        }
        realFunc = realFunc.replace("$0", actualPara1);
        return realFunc;
    }
}
