package com.xcstasy.tutorial.jvm.classloading;

public class SuperClass {
    static {
        System.out.println("SuperClass init");
    }

    public static int value = 123;


}
