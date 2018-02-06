package com.xcstasy.tutorial.algorithm.sort

class ShellSort : IntSort {
    override fun execute(source: IntArray) {
        val size = source.size
        var h = 1   //增量
        var inner: Int

        var temp: Int

        while (h <= size / 3) {//获取最大增量
            h = h * 3 + 1
        }
        while (h >= 1) {//每一级希尔排序之后，都要减小h，最后一次h == 1
            for (outer in h until size) {//从h处开始向后遍历
                temp = source[outer]
                inner = outer
                //前一个间隔的元素如果大于当前元素
                while (inner > h - 1 && source[inner - h] >= temp) {
                    //较大元素赋值到当前元素
                    source[inner] = source[inner - h]
                    //继续找前一个间隔
                    inner -= h
                }
                //之前被覆盖的值存储在temp中，现在赋值到应有的位置
                source[inner] = temp
            }
            h = (h - 1) / 3     //减小h
        }

    }

}