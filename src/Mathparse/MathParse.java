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

    private static HashMap<String, Double> var;

    public MathParse() {
        var = new HashMap<>();
        setVariable("pi",Math.PI);
        setVariable("e",Math.E);
    }

    public void setVariable(String varName, Double varValue) {
        var.put(varName, varValue);
    }

    public void replaceVariable(String varName, Double varValue) {
        var.replace(varName, varValue);
    }

    public Double getVariable(String varName) {
        if(!var.containsKey(varName)) {
            throw new NoSuchElementException("Error:Try get unexists " + "variable '" + varName + "'" );
        }
        return var.get(varName);
    }

    private Double requestValue(String varName){
        Scanner in = new Scanner(System.in);
        System.out.print(varName + " enter values  ");
        return in.nextDouble();
    }

    public double Parse(String s) {
        if(s.isEmpty())
            throw new NoSuchElementException("Empty expression");
        Result result = binaryFunc(s);
        if (!result.rest.isEmpty())
            throw new RuntimeException("Error: can't full parse \n " + "rest: " + result.rest);
        return result.acc;
    }

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


    private Result bracket(String s) {
        s = skipSpaces(s);
        if (s.charAt(0) == '(') {
            Result r = binaryFunc(s.substring(1));
            if (!r.rest.isEmpty()) {
                r.rest = r.rest.substring(1);
            } else {
                throw new NoSuchElementException("Expected closing bracket");
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
    private Result closeBracket(Result r) {
        if(!r.rest.isEmpty() && r.rest.charAt(0) ==')'){
            r.rest = r.rest.substring(1);
        } else
            throw new NoSuchElementException("Expected closing bracket");
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
    private Result processFunction(String func, double acc, Result r) {
        switch(func){
            case "log":
                return new Result(Math.log(acc)/Math.log(r.acc), r.rest);
            default:
                return new Result(customfunction(func),r.rest);
        }
    }

    private String skipSpaces(String s){
        return s.trim();
    }
    private Double customfunction(String func){
        System.out.println("Enter body of ur function");
        Scanner console = new Scanner(System.in);
        String bodyfunc=console.nextLine();
        double res=this.Parse(bodyfunc);
        return  res;
    }

}