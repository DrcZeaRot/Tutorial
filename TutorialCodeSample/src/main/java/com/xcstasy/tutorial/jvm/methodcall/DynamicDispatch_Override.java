package com.xcstasy.tutorial.jvm.methodcall;

/**
 * 动态分派
 */
public class DynamicDispatch_Override {
    static abstract class Human {
        protected abstract void sayHello();
    }

    static class Man extends Human{

        @Override
        protected void sayHello() {
            System.out.println("man says hello");
        }
    }

    static class Woman extends Human{
        @Override
        protected void sayHello() {
            System.out.println("woman says hello");
        }
    }

    public static void main(String[] args) {
        Human man = new Man();
        Human woman = new Woman();
        man.sayHello();
        woman.sayHello();
        man = new Woman();
        man.sayHello();
    }

    /**
     * 输出为：
     *          "man says hello"
     *          "woman says hello"
     *          "woman says hello"
     */
}
