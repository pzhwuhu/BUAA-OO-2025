import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numF = Integer.parseInt(scanner.nextLine());
        for (int i = 0;i < numF;i++) {
            ArrayList<String> define = new ArrayList<>();
            define.add(scanner.nextLine().replaceAll("[ \t]", ""));
            define.add(scanner.nextLine().replaceAll("[ \t]", ""));
            define.add(scanner.nextLine().replaceAll("[ \t]", ""));
            DiyFunc.addFunc(define);
        }
        String input = scanner.nextLine();
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        poly.print();
    }
}