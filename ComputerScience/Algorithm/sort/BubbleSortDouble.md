### 双向冒泡排序

>简单冒泡的双重版本，先从左到右将最大的冒泡，再从右向左将最小的冒泡

* 双重遍历，外层代表当前已排序的次数，内层进行双向的两次遍历，分别比较、交换最大/最小的
* 正向遍历就是简单冒泡遍历,逆向遍历从正向结束之后开始
* 逆向冒泡：比较array[j]和array[j - 1]，较小的放在左面
* 两端都是碰到第一个排序完成的元素后，就从头再次开始比较

>比较次数为 N*(N - 1)/2 ≈ N^2 / 2,交换次数平均为比较次数的一半 N^2 / 4,通常比简单冒泡交换少

[代码示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/algorithm/sort/BubbleSortDouble.kt)