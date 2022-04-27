package com.company;
import Mathparse.MathParse;
public class Main {

    public static void main(String[] args) {
        MathParse parser = new MathParse();
        String[] expressions ={"2*x(t1,z)","2*f(3","2*3"};
        for (String expr:expressions
             ) {
            try{
                double s=parser.Parse(expr);
                System.out.println(s);
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }

        }
//        double s=parser.Parse(expressions[0]);
        //  System.out.println(s);

    }
}
