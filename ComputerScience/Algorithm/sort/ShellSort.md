### 希尔排序

>基于插入排序、适合中等规模的数据、不适合超大数据、容易实现。

>一些专家提倡：差不多任何排序工作在开始时都可以使用希尔排序，若在实际中证明它不够快，再换成诸如快速排序这样的高级排序算法。

时间复杂度：O(N * (logN)^2)。通常在O(N^(3/2))~O(N^(7/6))

#### 理论
```
    数据源：7 , 10 , 1 , 9 , 2 , 5 , 8 , 6 , 4 , 3
    以4为增量，将源分为多个部分：
     0 4 8   1  5 9   2 6   3 7
    (7,2,4),(10,5,3),(1,8),(9,6)
    对每个小部分排序，让数组"基本有序"
    (2,4,7),(3,5,10),(1,8),(6,9)
    实际为：2,3,1,6,4,5,8,9,7,10
```

* 增量排序
    >1. 插入排序在不够理想的情况下，对数据的移动、复制次数太多。
    >2. 希尔排序通过"加大插入排序中元素之间的间隔、在有间隔的元素中进行插入排序，使得数据能大跨度移动"
    >3. 每级希尔排序之后，减小增量再次排序，直到增量为1

* 常见间隔序列
    1. Knuth间隔序列：3*h+1 =>{4,13,40,121,364,1093，3280}


[代码示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/algorithm/sort/ShellSort.kt)