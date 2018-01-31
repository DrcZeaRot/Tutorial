### 选择排序

##### 思路

    for outer in 0 .. n -1
        for inner in 0 .. n -2

* 遍历一次，记录当前遍历最小的值，然后将它放在此次遍历的头号位置
* 向后移动一位继续遍历，每次都把最小的放在最前面

>比较次数为 N*(N - 1)/2 ≈ N^2 / 2,交换次数为N

不变性
>每次遍历，前outer项都是有序的

[代码示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/algorithm/sort/SelectionSort.kt)