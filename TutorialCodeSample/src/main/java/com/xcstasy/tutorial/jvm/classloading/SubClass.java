package com.xcstasy.tutorial.jvm.classloading;

class SubClass extends SuperClass {
    static {
        System.out.println("SubClass init");
    }
}