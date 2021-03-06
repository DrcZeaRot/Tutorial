### 算法

>算法（Algorithm）是指解题方案的准确而完整的描述，是一系列解决问题的清晰指令，算法代表着用系统的方法描述解决问题的策略机制。

##### 算法特征：
* 有穷性 （Finiteness）
>算法的有穷性是指算法必须能在执行有限个步骤之后终止；

* 确切性 (Definiteness)
> 算法的每一步骤必须有确切的定义；

* 输入项 (Input)
>一个算法有0个或多个输入，以刻画运算对象的初始情况，所谓0个输入是指算法本身定出了初始条件；

* 输出项 (Output)
>一个算法有一个或多个输出，以反映对输入数据加工后的结果。没有输出的算法是毫无意义的；

* 可行性 (Effectiveness)
>算法中执行的任何计算步骤都是可以被分解为基本的可执行的操作步，即每个计算步都可以在有限时间内完成（也称之为有效性）。


算法设计目标
- 正确性
- 可读性
- 健壮性
- 高时间效率
- 高空间效率

##### 算法分析评定

>同一问题可用不同算法解决，而一个算法的质量优劣将影响到算法乃至程序的效率。算法分析的目的在于选择合适算法和改进算法。

>一个算法的评价主要从[时间复杂度](complexity/TimeComplexity.md)和[空间复杂度](complexity/SpaceComplexity.md)来考虑。

* 时间复杂度
>算法的时间复杂度是指执行算法所需要的计算工作量。一般来说，计算机算法是问题规模n 的函数f(n)，算法的时间复杂度也因此记做。
T(n)=Ο(f(n))
因此，问题的规模n 越大，算法执行的时间的增长率与f(n) 的增长率正相关，称作渐进时间复杂度（Asymptotic Time Complexity）。

* 空间复杂度
>算法的空间复杂度是指算法需要消耗的内存空间。其计算和表示方法与时间复杂度类似，一般都用复杂度的渐近性来表示。同时间复杂度相比，空间复杂度的分析要简单得多。

* 正确性
>算法的正确性是评价一个算法优劣的最重要的标准。

* 可读性
>算法的可读性是指一个算法可供人们阅读的容易程度。

* 健壮性
>健壮性是指一个算法对不合理数据输入的反应能力和处理能力，也称为容错性。