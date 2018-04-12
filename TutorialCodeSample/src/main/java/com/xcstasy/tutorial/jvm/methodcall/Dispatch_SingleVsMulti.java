package com.xcstasy.tutorial.jvm.methodcall;

/**
 * 单分派Vs多分派
 */
public class Dispatch_SingleVsMulti {

    static abstract class Company{}

    static class QQ extends Company{
    }

    static class _360 extends Company{
    }

    public static class Father {

        public void hardChoice(Company arg) {
            System.out.println("father chooses company");
        }

        public void hardChoice(QQ arg) {
            System.out.println("father chooses qq");
        }

        public void hardChoice(_360 arg) {
            System.out.println("father chooses 360");
        }
    }

    public static class Son extends Father {

        public void hardChoice(Company arg) {
            System.out.println("son chooses company");
        }

        public void hardChoice(QQ arg) {
            System.out.println("son chooses qq");
        }

        public void hardChoice(_360 arg) {
            System.out.println("son chooses 360");
        }
    }

    public static void main(String[] args) {
        Father father = new Father();
        Father son = new Son();
        _360 qihu = new _360();
        QQ qq = new QQ();
        Company company = qq;
        father.hardChoice(qihu);
        father.hardChoice(company);
        son.hardChoice(qq);
        son.hardChoice(company);
    }

    /**
     * 结果：
     *      "father chooses 360"
     *      "father chooses company"
     *      "son chooses qq"
     *      "son chooses company"
     *
     *
     *  静态分派发生在：
     *      1. 静态类型是Father还是Son
     *      2. 方法参数是Company还是QQ还是360
     *
     *  1.这次选择结果的最终产物：产生了两条invokevirtual指令
     *  2.两条指令的参数，分别为：常量池中指向Father.hardChoice(360) 、Father.hardChoice(QQ)方法的符号引用
     *
     *  此处，根据两个宗量进行了选择。
     *  所以：Java语言的静态分派属于：多分派类型
     *
     *  动态分派发生在：
     *      1. 执行son.hardChoice(qq)时
     *          * 由于编译期已经确定了目标方法的签名必须为hardChoice(QQ)
     *          * 虚拟机此时不会关心传递进来的参数"qq"究竟是"腾讯QQ"还是"奇瑞QQ"
     *          * 因为此时：参数的静态类型、实际类型，都对方法的选择不会构成任何影响
     *          * 唯一可以影响虚拟机选择的因素，只有：
     *              * 此方法的接受者，实际类型是Father还是Son
     *  此处，由于只有一个宗量作为选择依据
     *  所以：Java语言的动态分派属于：单分派类型
     */
}
