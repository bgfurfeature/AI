package com.iteview;

/**
 * Created by C.J.YOU on 2017/3/3.
 * 面试巧搭
 */
public class Test {

    public static void main(String args[]) {


        // 1. B 继承 A，C 继承 B，我们能将 B 转换为 C 么？如 C = (C) B
        // java.lang.ClassCastException: com.iteview.B cannot be cast to com.iteview.C
        /*B b = new B();
        C c = (C)b;
        System.out.println(c.toString());*/

        // 2.
        byte a = 127;
        byte b = 127;
        b += a; // ok  -2
        System.out.println(b); // -2

        System.out.println (3 * 0.1); // 0.30000000000000004
        System.out.println(3 * 0.1 == 0.3); // false


    }
}
