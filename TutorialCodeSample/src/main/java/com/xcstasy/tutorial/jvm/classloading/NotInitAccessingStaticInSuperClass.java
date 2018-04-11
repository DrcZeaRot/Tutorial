package com.xcstasy.tutorial.jvm.classloading;

/**
 * 通过子类，引用父类的静态字段，不会导致子类初始化
 * 输出为： "SuperClass init" "123"
 * "SubClass init"不会输出
 */
public class NotInitAccessingStaticInSuperClass {
    public static void main(String[] args) {
        System.out.println(SubClass.value);
    }
}