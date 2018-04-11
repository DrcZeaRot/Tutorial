package com.xcstasy.tutorial.jvm.classloading;

/**
 * 常量在编译阶段会存入调用类的常量池中，本质上并没有直接引用到定义常量的类，引用常量不会触发定义常量的类的初始化
 */
public class NotInitConstantValue {
    public static void main(String[] args) {
        System.out.println(ConstClass.HELLO_WORLD);
    }
}
