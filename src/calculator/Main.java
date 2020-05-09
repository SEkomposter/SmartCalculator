package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    static Map<String, String> vars = new LinkedHashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;
        while (scanner.hasNextLine()) {
            int sum = 0;
            input = scanner.nextLine().replaceAll("\\s+", "").replaceAll("\\A\\s*\\+", "").replaceAll("(--)+", "+").replaceAll("\\A-", "0-").replaceAll("\\+{2,}", "+");
            if (input.equals("/exit")) {
                System.out.println("Bye!");
            } else if (input.equals("/help")){
                System.out.println("The program support the addition + and subtraction - operators, and variables");
            } else if (input.startsWith("/")){
                System.out.println("Unknown command");
            }
            else {
                if (input.equals("")) {
                    continue;
                } else if(input.matches("\\w+(=\\-?\\w*)+")){
                    addVar(input);
                //} else if(input.matches("[a-zA-Z]+")){
                    //showVar(input);
                 //   calculate(makePostfix(input));
                } else if (isExpInvalid(input)) {
                    System.out.println("Invalid expression");
                } else if(input.matches("\\(*(([\\+-]?[0-9]+)|([a-zA-Z]+))(([\\+\\-\\/\\*]\\(*[a-zA-Z]+)?\\)*(\\(*[\\+\\-\\/\\*]\\(*[0-9]+)?\\)*)*")){
                    calculate(makePostfix(input));
                } else if(input.matches("[\\+-]?\\d+([\\+-]\\d*)*")){
                    calculate(makePostfix(input));
                } else {
                    System.out.println("Invalid expression");
                }
            }
        }
    }

    private static String makePostfix(String incoming) {
        String[] exp = splitExpression(incoming);
        StringBuilder result = new StringBuilder();
        Deque<String> stack = new ArrayDeque<>();

        for (int counter = 0; counter < exp.length; counter++)
            if (exp[counter].matches("[\\da-zA-Z]+")){
                result.append(exp[counter] + " ");
            } else if (stack.isEmpty()||stack.peekLast().matches("\\(")) {
                stack.offerLast(exp[counter]);
            } else if (stack.peekLast().matches("[\\+\\-]") && exp[counter].matches("[\\*\\/]")) {
                stack.offerLast(exp[counter]);
            } else if ((stack.peekLast().matches("[\\*\\/]") && exp[counter].matches("[\\*\\/\\+\\-]")) ||
                    (stack.peekLast().matches("[\\+\\-]") && exp[counter].matches("[\\+\\-]"))) {
                while (!stack.isEmpty() && (!(stack.peekLast().matches("[\\+\\-]") && !exp[counter].matches("[\\*\\/]")) ||
                        exp[counter].matches("[\\+\\-]")) && !stack.peekLast().matches("[\\(]")) {
                    result.append(stack.pollLast() + " ");
                    if (stack.isEmpty()) break;
                }
                stack.offerLast(exp[counter]);
            } else  if (exp[counter].matches("\\(")){
                stack.offerLast(exp[counter]);
            }
            else if (exp[counter].matches("\\)")) {
                while (!stack.peekLast().matches("\\(")) {
                    result.append(stack.pollLast() + " ");
                    if (stack.isEmpty()) break;
                }
                stack.pollLast();
            }
        while (!stack.isEmpty()) {
            result.append(stack.pollLast() + " ");
        }
        return result.toString();
    }

    private static String[] splitExpression (String input) {
        StringBuilder digit = new StringBuilder();
        boolean isPrevDigit = false;
        StringBuilder sb = new StringBuilder();
        for (char c: input.toCharArray()) {
            if (String.valueOf(c).matches("[\\da-zA-Z]")) {
                digit.append(c);
                isPrevDigit = true;
            } else if (String.valueOf(c).matches("[\\+\\-\\*\\/\\(\\)]") && isPrevDigit) {
                sb.append(digit.toString() + " ");
                digit.replace(0,digit.length(),"");
                sb.append(String.valueOf(c) + " ");
                isPrevDigit = false;
            } else if (String.valueOf(c).matches("[\\+\\-\\*\\/\\(\\)]")) {
                sb.append(String.valueOf(c) + " ");
            }
        }
        sb.append(digit.toString());
        return sb.toString().split("\\s");
    }

    private static boolean isExpInvalid(String input) {
        if (Pattern.compile("[\\*\\/]{2,}").matcher(input).find()||
                unpairedParenthesis(input))
            return true;
        else
            return false;
    }

    private static boolean unpairedParenthesis(String input) {
        char[] charsInput = input.toCharArray();
        int rightP = 0, leftP = 0;
        for (char c:charsInput) {
            if (c == '(') leftP++;
            else if (c == ')') rightP++;
        }
        return leftP != rightP? true: false;
    }

    private static String varsToInts(String input) {
        String[] vars;
        String[] signs;
        StringBuilder sb = new StringBuilder();
        Boolean varExist = true;
        vars = input.split("[+\\-]");
        signs = input.split("[a-zA-Z0-9]+");
        if (vars[0].matches("\\d+")||isVarExist(vars[0])) sb.append(getVarValue(vars[0]));
        else varExist = false;
        if (varExist) {
            for (int i = 1; i < vars.length; i++) {
                if (vars[i].matches("\\d+")||isVarExist(vars[i])) sb.append(signs[i] + getVarValue(vars[i]));
                else {
                    varExist = false;
                    break;
                }
            }
        }
        return varExist? sb.toString() : "";
    }

    private static Boolean isVarExist (String input) {
        if(!vars.containsKey(input)) System.out.println("Unknown variable");
        return vars.containsKey(input);
    }

    private static void showVar(String input) {
        if (isVarExist(input)) System.out.println(vars.getOrDefault(input, null));
    }

    public static void calculate (String exp) {
        Deque<String> sumStack = new ArrayDeque<>();
        String[] output = exp.split("\\s");
        BigInteger a  = BigInteger.ZERO, b = BigInteger.ZERO, result = BigInteger.ZERO;
        for (String s: output) {
            if (s.matches("\\d+")) {
                sumStack.offerLast(s);
            } else if (s.matches("[a-zA-Z]+")){
                if (isVarExist(s))
                    sumStack.offerLast(varsToInts(s));
            } else if (s.matches("[\\+\\-\\*\\/]")) {
                b = new BigInteger(sumStack.pollLast());
                a = new BigInteger(sumStack.pollLast());
                switch (s){
                    case "+": result = a.add(b);
                        break;
                    case "-": result = a.subtract(b);
                        break;
                    case "*": result = a.multiply(b);
                        break;
                    case "/": result = a.divide(b);
                        break;
                }
                sumStack.offerLast(result.toString());
            }
        }
        if (!sumStack.isEmpty())
            System.out.println(sumStack.pollLast());
    }

    public static void addVar (String input) {
        String[] inputs;
        inputs = input.split("=");
        if (inputs[0].matches("[a-zA-Z]+")){
            if (inputs.length > 2)
                System.out.println("Invalid assignment");
            else if (inputs[1].matches("\\-?\\d+"))
                vars.put(inputs[0],inputs[1]);
            else if (inputs[1].matches("[a-zA-Z]+")) {
                vars.put(inputs[0],getVarValue(inputs[1]));
            } else if (!inputs[1].matches("\\d+"))
                System.out.println("Invalid assignment");
        } else if (!inputs[0].matches("[a-zA-Z]+")) System.out.println("Invalid identifier");

    }

    private static String getVarValue(String input) {
        return vars.getOrDefault(input, null).toString();
    }
}
