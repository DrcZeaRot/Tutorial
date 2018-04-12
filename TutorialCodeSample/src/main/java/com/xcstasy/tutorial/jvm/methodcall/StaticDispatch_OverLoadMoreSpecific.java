package com.xcstasy.tutorial.jvm.methodcall;

import java.io.Serializable;

/**
 * 稍微复杂的静态分派
 */
public class StaticDispatch_OverLoadMoreSpecific {

    public static void sayHello(Object arg) {
        System.out.println("hello Object");
    }

    public static void sayHello(int arg) {
        System.out.println("hello int");
    }

    public static void sayHello(long arg) {
        System.out.println("hello long");
    }

    public static void sayHello(Character arg) {
        System.out.println("hello Character");
    }

    public static void sayHello(char arg) {
        System.out.println("hello char");
    }

    public static void sayHello(char... arg) {
        System.out.println("hello char...");
    }

    public static void sayHello(Serializable arg) {
        System.out.println("hello Serializable");
    }

    public static void main(String[] args) {
        sayHello('a');
    }

    /**
     *  1.直接运行，输出"hello char"
     *  2.注释掉char类型，输出"hello int"(char类型自动转换为int的97)
     *  3.再注释掉int类型，输出"hello long"(转化为int之后，进一步转化为长整型97L)
     *  4.再注释掉long类型，输出"hello Character"(触发自动装箱)
     *  5.再注释掉Character类型，输出"Serializable"(Character实现了Serializable接口)
     *      * 如果此时还有另一个Character实现的接口java.lang.Comparable<Character>存在，
     *      * 此时，两个接口优先级相同，无法确定自动转换，会提示类型模糊，拒绝编译。
     *  6.再注释掉Serializable类型，输出"hello Object"
     *      * (装箱后，转化为父类<多个父类则从下向上搜索继承关系，找最近的>)
     *      * 即使调用时传入null，此规则依然适用
     *  7.再注释掉Object，只剩char...边长参数，输出"hello char..."
     */
}
