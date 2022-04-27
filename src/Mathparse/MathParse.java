package Mathparse;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MathParse {
    /**
     *class for containing remaining string and accum
     */
    private class Result {
        public double acc; // Аккумулятор
        public String rest; // остаток строки, которую мы еще не обработали
        public Result(double v, String r) {
            this.acc = v;
            this.rest = r;
        }
    }

    /**
     * Container for variables
     */
    private static HashMap<String, Double> var;

    /**
     Constructor
     */
    public MathParse() {
        var = new HashMap<>();
        setVariable("pi",Math.PI);
        setVariable("e",Math.E);
    }

    /**
     * Add variable to our container
     * @param varName name of variable
     * @param varValue value of variable
     */
    public void setVariable(String varName, Double varValue) {
        var.put(varName, varValue);
    }

    /**
     * replacing variable in container
     * @param varName name of variable
     * @param varValue value of variable
     */
    public void replaceVariable(String varName, Double varValue) {
        var.replace(varName, varValue);
    }

    /**
     * getting variable
     * @param varName name of variable
     * @return variable
     */
    public Double getVariable(String varName) {
        if(!var.containsKey(varName)) {
            throw new NoSuchElementException("Error:Try get unexists " + "variable '" + varName + "'" );
        }
        return var.get(varName);
    }

    /**
     * requesting for value of variable
     * @param varName name of variable
     * @return reading value of variable from console
     */
    private Double requestValue(String varName){
        Scanner in = new Scanner(System.in);
        System.out.print(varName + " enter values  ");
        return in.nextDouble();
    }

    /**
     * Parse entered expression from user
     * @param s expression
     * @return result of expression
     */
    public double Parse(String s) {
        if(s.isEmpty())
            throw new NoSuchElementException("Empty expression");
        Result result = binaryFunc(s);
        if (!result.rest.isEmpty())
            throw new RuntimeException("Error: can't full parse \n " + "rest: " + result.rest);
        return result.acc;
    }

    /**
     * main function of parsing expression
     * @param s expression
     * @return intermediate result of parsing
     */
    private Result binaryFunc(String s) {
        Result cur;

        if(s.charAt(0) == '~'){
            cur = plusMinus(s.substring(1));

            cur.acc = ~ (int)cur.acc;
            return cur;
        }

        cur = plusMinus(s);
        double acc = cur.acc;

        cur.rest = skipSpaces(cur.rest);

        while(cur.rest.length() > 0){
            if(!(cur.rest.charAt(0) == '&' || cur.rest.charAt(0) == '|' || cur.rest.charAt(0) == '~')) {
                break;
            }

            char sign = cur.rest.charAt(0);
            String next = cur.rest.substring(1);
            cur = plusMinus(next);

            if(sign == '&')
                acc = (int)acc & (int)cur.acc;
            else
                acc = (int)acc | (int)cur.acc;
        }

        return new Result(acc,cur.rest);
    }

    /**
     *
     * @param s
     * @return
     */
    private Result plusMinus(String s) {
        Result cur = mulDiv(s);
        double acc = cur.acc;

        cur.rest = skipSpaces(cur.rest);

        while(cur.rest.length() > 0){
            if(!(cur.rest.charAt(0) == '+' || cur.rest.charAt(0) == '-')) {
                break;
            }

            char sign = cur.rest.charAt(0);
            String next = cur.rest.substring(1);

            cur = binaryFunc(next);

            if(sign == '+')
                acc+=cur.acc;
            else
                acc-=cur.acc;
        }
        return new Result(acc, cur.rest);
    }

    private Result mulDiv(String s) {
        Result cur = exponentiation(s);
        double acc = cur.acc;

        cur.rest = skipSpaces(cur.rest);

        while(true){
            if(cur.rest.length() == 0) {
                return cur;
            }

            char sign = cur.rest.charAt(0);
            if(sign != '*' && sign != '/' && sign != '%' && sign != '\\') {
                return cur;
            }

            String next = cur.rest.substring(1);
            Result right = exponentiation(next);
            switch(sign){
                case '*':
                    acc*=right.acc;
                    break;
                case '/':
                    acc/=right.acc;
                    break;
                case '%':
                    acc%=right.acc;
                    break;
                case '\\': // целочисленное деление
                    acc = (acc - acc % right.acc)/right.acc;
                    break;
            }
            cur = new Result(acc,right.rest);
        }
    }

    /**
     * finding exponentiation func
     * @param s rest expr
     * @return intermediate result
     */
    private Result exponentiation(String s) {
        Result cur = bracket(s);
        double acc = cur.acc;

        cur.rest = skipSpaces(cur.rest);

        while(true){
            if(cur.rest.length() == 0) {
                return cur;
            }
            if(cur.rest.charAt(0) != '^') {
                break;
            }

            String next = cur.rest.substring(1);
            cur = bracket(next);
            cur.acc = Math.pow(acc,cur.acc);
        }
        return cur;
    }

    /**
     * parse values inside of function bracket
     * @param s rest expression
     * @return intemediate result
     */
    private Result bracket(String s) {
        s = skipSpaces(s);
        if (s.charAt(0) == '(') {
            Result r = binaryFunc(s.substring(1));
            if (!r.rest.isEmpty()) {
                r.rest = r.rest.substring(1);
            } else {
                throw new NoSuchElementException("Expected bracket in the end");
            }
            return r;
        }
        return functionVariable(s);
    }

    private Result functionVariable(String s) {
        String f = "";
        int i = 0;
        // ищем название функции или переменной
        // имя обязательно должна начинаться с буквы
        while (i < s.length() && (Character.isLetter(s.charAt(i)) || (Character.isDigit(s.charAt(i)) && i > 0))) {
            f += s.charAt(i);
            i++;
        }
        if(Character.isDigit(s.charAt(0)) && s.length()>1 &&  Character.isLetter(s.charAt(1)))
            throw new IllegalArgumentException("Invalid name of variable");
        if (!f.isEmpty()) { // если что-нибудь нашли
            if (s.length() > i && s.charAt( i ) == '(') {
                // и следующий символ скобка значит - это функция
                Result r = binaryFunc(s.substring(f.length()+1));

                if(!r.rest.isEmpty() && r.rest.charAt(0) == ','){
                    // если функция с двумя параметрами
                    double acc = r.acc;
                    Result r2 = binaryFunc(r.rest.substring(1));

                    r2 = closeBracket(r2);
                    return processFunction(f, acc, r2);
                } else {
                    r = closeBracket(r);
                    return processFunction(f, r);
                }
            } else { // иначе - это переменная
                if(!var.containsKey(f)){
                    setVariable(f, requestValue(f));
                }

                return new Result(getVariable(f), s.substring(f.length()));
            }
        }
        return num(s);
    }

    /**
     * finding close bracket for function
     * @param r intermediate result for func values
     * @return intermediate result
     */
    private Result closeBracket(Result r) {
        if(!r.rest.isEmpty() && r.rest.charAt(0) ==')'){
            r.rest = r.rest.substring(1);
        } else
            throw new NoSuchElementException("Expected  bracket in the end ");
        return r;
    }

    private Result num(String s) {
        int i = 0;
        int dot_cnt = 0;
        boolean negative = false;
        // число также может начинаться с минуса
        if(s.charAt(0) == '-'){
            negative = true;
            s = s.substring(1);
        }
        // разрешаем только цифры и точку
        while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
            // но также проверяем, что в числе может быть только одна точка!
            if (s.charAt(i) == '.' && ++dot_cnt > 1) {
                throw new NumberFormatException("not valid number '" + s.substring(0, i + 1) + "'");
            }
            i++;
        }
        if(i == 0){ // что-либо похожее на число мы не нашли
            throw new NoSuchElementException("can't get valid number in '" + s + "'" );
        }

        double dPart = Double.parseDouble(s.substring(0, i));
        if(negative){
            dPart = -dPart;
        }
        String restPart = s.substring(i);

        return new Result(dPart, restPart);
    }

    /**
     * finding name of function or requesting for custom
     * @param func name of func
     * @param r intermediate result
     * @return intermediate result of func
     */
    private Result processFunction(String func, Result r) {
        switch (func) {
            case "sin":
                return new Result(Math.sin(r.acc), r.rest);
            case "sinh": // гиперболический синус
                return new Result(Math.sinh(r.acc), r.rest);
            case "cos":
                return new Result(Math.cos(r.acc), r.rest);
            case "cosh": // гиперболический косинус
                return new Result(Math.cosh(r.acc), r.rest);
            case "tan":
                return new Result(Math.tan(r.acc), r.rest);
            case "tanh": // гиперболический тангенс
                return new Result(Math.tanh(r.acc), r.rest);
            case "ctg":
                return new Result(1/Math.tan(r.acc), r.rest);
            case "sec": // секанс
                return new Result(1/Math.cos(r.acc), r.rest);
            case "cosec": // косеканс
                return new Result(1/Math.sin(r.acc), r.rest);
            case "abs":
                return new Result(Math.abs(r.acc), r.rest);
            case "ln":
                return new Result(Math.log(r.acc), r.rest);
            case "lg":
                return new Result(Math.log10(r.acc), r.rest);
            case "sqrt":
                return new Result(Math.sqrt(r.acc), r.rest);
            default:
                return new Result(customfunction(func),r.rest);
        }
    }

    /**
     * finding name of function if there are two param
     * @param func name of func
     * @param acc accumulator
     * @param r intermediate result
     * @return intermediate result of func
     */
    private Result processFunction(String func, double acc, Result r) {
        switch(func){
            case "log":
                return new Result(Math.log(acc)/Math.log(r.acc), r.rest);
            default:
                return new Result(customfunction(func),r.rest);
        }
    }

    /**
     * delete spaces from expr
     * @param s expression
     * @return expression without spaces
     */
    private String skipSpaces(String s){
        return s.trim();
    }

    /**
     * requesting for body of custom func
     * @param func func name
     * @return result of parsing custom fubc
     */
    private Double customfunction(String func){
        System.out.println("Enter body of ur function");
        Scanner console = new Scanner(System.in);
        String bodyfunc=console.nextLine();
        double res=this.Parse(bodyfunc);
        return  res;
    }

}