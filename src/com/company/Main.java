package com.company;
import Mathparse.MathParse;
public class Main {

    public static void main(String[] args) {
        MathParse parser = new MathParse();
        String[] expressions ={"2*v(x)*50"};
        double s=parser.Parse(expressions[0]);
        System.out.println(s);

    }
}
