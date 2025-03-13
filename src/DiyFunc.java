import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DiyFunc {
    //由序号得到递推表达式
    private HashMap<Character, String> funcMap = new HashMap<>();
    //由函数名得到对应的递推HashMap
    private static final HashMap<String, HashMap<Character, String>> functions = new HashMap<>();
    private static final HashMap<String, ArrayList<String>> paraMap = new HashMap<>();
    private static String f0;
    private static String f1;
    private static String fn;

    //通过三行输入来得到一个函数
    public static void addFunc(ArrayList<String> define) {
        String s = define.get(0);
        String funcName = String.valueOf(s.charAt(0));
        //获取参数列表
        String para1 = String.valueOf(s.charAt(5));
        ArrayList<String> paraList = new ArrayList<>();
        paraList.add(para1);
        if (s.charAt(6) == ',') {
            String para2 = String.valueOf(s.charAt(7));
            paraList.add(para2);
        }
        paraMap.put(funcName, paraList);
        //获取递推式和初始化式子
        HashMap<Character, String> newFuncMap = new HashMap<>();
        for (String func : define) {
            int i = 0;
            while (func.charAt(i) != '=') {
                i++;
            }
            String expr = func.substring(i + 1);
            newFuncMap.put(func.charAt(2), expr);
        }
        functions.put(funcName, newFuncMap);
    }

    public static String deployFunc(int n, String name, ArrayList<Factor> actualParam) {
        HashMap<Character, String> funcMap = functions.get(name);
        fn = funcMap.get('n');
        f1 = funcMap.get('1');
        f0 = funcMap.get('0');
        ArrayList<String> fnParaList = getParam(fn);
        ArrayList<String> formParaList = paraMap.get(name);
        String formPara1 = formParaList.get(0);
        if (actualParam.size() == 2) {
            String formPara2 = formParaList.get(1);
            String func = recurrence2(n, formPara1, formPara2, formParaList, fnParaList);
            String para1 = actualParam.get(0).toString();
            String para2 = actualParam.get(1).toString();
            //System.out.println(func);
            return func.replace(formPara1, "$0").replace(formPara2, "$1")
                    .replace("$0", "(" + para1 + ")").replace("$1", "(" + para2 + ")");
        }
        else {
            String func = recurrence1(n, formPara1, formParaList, fnParaList);
            String para1 = actualParam.get(0).toString();
            //System.out.println(func);
            return func.replace(formPara1, "(" + para1 + ")");
        }
    }

    public static String recurrence1(int i, String a,
        ArrayList<String> formParaList, ArrayList<String> fnParaList) {
        String formPara = formParaList.get(0);
        if (i == 0) {
            return f0.replace(formPara, "(" + a + ")");
        }
        else if (i == 1) {
            return f1.replace(formPara, "(" + a + ")");
        }
        else {
            String a1 = fnParaList.get(0);
            String f2 = "(" + recurrence1(i - 1, a1, formParaList, fnParaList) + ")";
            String sb1 = "f{n-1}(" + a1 + ")";
            String a2 = fnParaList.get(1);
            String f3 = "(" + recurrence1(i - 2, a2, formParaList, fnParaList) + ")";
            String sb2 = "f{n-2}(" + a2 + ")";
            return fn.replace(sb1, f2).replace(sb2, f3).replace(formPara, "(" + a + ")");
        }
    }

    public static String recurrence2(int i, String a, String b,
        ArrayList<String> formParaList, ArrayList<String> fnParaList) {
        String formPara1 = formParaList.get(0);
        String formPara2 = formParaList.get(1);
        if (i == 0) {
            return f0.replace(formPara1, "$0").replace(formPara2, "$1")
                    .replace("$0", "(" + a + ")").replace("$1", "(" + b + ")");
        }
        else if (i == 1) {
            return f1.replace(formPara1,"$0").replace(formPara2,"$1")
                    .replace("$0", "(" + a + ")").replace("$1", "(" + b + ")");
        }
        else {
            String a1 = fnParaList.get(0);
            String b1 = fnParaList.get(1);
            String f2 = "(" + recurrence2(i - 1, a1, b1, formParaList, fnParaList) + ")";
            String sb1 = "f{n-1}(" + a1 + "," + b1 + ")";
            String a2 = fnParaList.get(2);
            String b2 = fnParaList.get(3);
            String f3 = "(" + recurrence2(i - 2, a2, b2, formParaList, fnParaList) + ")";
            String sb2 = "f{n-2}(" + a2 + "," + b2 + ")";
            return fn.replace(sb1, f2).replace(sb2, f3).replace(formPara1, "$0").
                    replace(formPara2,"$1").replace("$0","(" + a + ")").replace("$1","(" + b + ")");
        }
    }

    public static ArrayList<String> getParam(String fn) {
        ArrayList<String> fnParaList = new ArrayList<>();
        int flag = 0;
        for (int i = 0;i < fn.length();i++) {
            if (fn.charAt(i) == '}') {
                i += 2;
                flag++;
                StringBuilder sb = new StringBuilder();
                while (i < fn.length() && flag > 0) {
                    char c = fn.charAt(i);
                    if (c == '(') { flag++; }
                    else if (c == ')') { flag--; }
                    if (flag > 0) { sb.append(c); }
                    i++;
                }
                String[] paras = sb.toString().split(",");
                fnParaList.addAll(Arrays.asList(paras));
            }
        }
        return fnParaList;
    }
}
