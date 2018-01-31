### 简单冒泡排序

>概念上最简单的排序，由于运行效率很低，只用于初步理解算法

##### 思路

    for outer in 0 .. n -1
        for inner in 0 .. n -2

* 双层遍历，外层代表当前已排序的次数，内层进行遍历并比较、交换
* 比较array[j]和array[j+1],左边大则交换
* 碰到第一个排序完成的元素后，从头再次开始比较

不变性
>每次遍历，最右侧的outer项都是有序的。

>比较次数为 N*(N - 1)/2 ≈ N^2 / 2,交换次数平均为比较次数的一半 N^2 / 4

[代码示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/algorithm/sort/BubbleSortSimple.kt)