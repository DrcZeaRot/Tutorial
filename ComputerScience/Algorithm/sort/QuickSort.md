### 快速排序

#### 根本机制：划分
>划分数据就是：把数组分为两组，是所有关键字大于特定值的数据在一组，关键字小于特定值的数据在另一组

划分思路：
```
    首先，选择一个参考值Pivot
    两个指针leftPtr、rightPtr，分别从两端向中间移动。
    leftPtr发现比Pivot大的值，停止移动。
    rightPtr发现比Pivot小的值，停止移动。
    然后交换leftPtr和rightPtr的值，他们俩就都到正确的位置。
    交换之后，继续向中间移动两个指针、停止、交换。
    最终两个指针相遇，划分过程结束。
```

划分的复杂度为O(N)

#### 快速排序

* 对数据源进行划分、递归。
* 每次递归都将数据分为分别大于/小于参考值的两组
* 分解到最后，数据就已经排序完成

[代码示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/algorithm/sort/QuickSort.kt)