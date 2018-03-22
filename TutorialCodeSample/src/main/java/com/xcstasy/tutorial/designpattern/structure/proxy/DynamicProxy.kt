package com.xcstasy.tutorial.designpattern.structure.proxy

import com.xcstasy.tutorial.util.logW
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy


interface Subject {
    fun doSomething(str: String)
}

class RealSubject : Subject {
    override fun doSomething(str: String) {
        "Do Something $str".logW()
    }
}

class MyInvocationHandler(private val target: Any) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>): Any {
        return method.invoke(this.target, args)
    }
}

interface IAdvice {
    fun execute()
}

class BeforeAdvice : IAdvice {
    override fun execute() {
        "前置通知被执行".logW()
    }
}

object SubjectDynamicProxy{
    private val advice: IAdvice = BeforeAdvice()

    fun newProxyInstance(subject: Subject): Subject {
        val handler: InvocationHandler = MyInvocationHandler(subject)
        val classLoader = subject.javaClass.classLoader
        val interfaces = subject.javaClass.interfaces
        //此处可以进行AOP
        advice.execute()
        return Proxy.newProxyInstance(classLoader, interfaces, handler) as Subject
    }
}

fun main(args: Array<String>) {
    val subject: Subject = RealSubject()
    val proxy: Subject = SubjectDynamicProxy.newProxyInstance(subject)
    proxy.doSomething("Finish")
}