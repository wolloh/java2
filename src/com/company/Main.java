package com.company;
import Mathparse.MathParse;
public class Main {

    public static void main(String[] args) {
        /*MathParse parser = new MathParse();
        String[] expressions ={"2*v(x)*50"};
        double s=parser.Parse(expressions[0]);
        System.out.println(s);*/
        MathParse parser = new MathParse();
        String[] expressions = {"2+4*3-8^3",
                "4*sin(0.5)+2*cos(2*0.5)",
                "log(64,2)*lg(100)",
                "63 & 95",
                "2*(5-7",
                "x*5",
                "",
                "2*f(x,y)*50",
                "e^2+1",
                "6.78.-7"};

        for(String expression:expressions){
            System.out.print(expression+"  ");
            try{
                double str = parser.Parse(expression);
                System.out.print(str+"\n");
            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}
