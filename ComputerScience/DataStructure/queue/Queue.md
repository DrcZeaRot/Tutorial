### 队列

>队列同样是一种特殊的线性表，其插入和删除的操作分别在表的两端进行，队列的特点就是先进先出(First In First Out)。我们把向队列中插入元素的过程称为入队(Enqueue)，删除元素的过程称为出队(Dequeue)并把允许入队的一端称为队尾，允许出的的一端称为队头，没有任何元素的队列则称为空队。

##### 常见实现

1. 顺序队列
    * 如果使用顺序表实现，在执行出队操作时，会对整个表进行移动，
    时间复杂度为O(N)
    * 通常实现为循环队列，增加商标front和下标rear，出队操作只需要改变front和rear
    时间复杂度为O(1)

2. 链式队列


[代码示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/datastructure/queue/Queue.kt)

##### 参考
[java数据结构与算法之（Queue）队列设计与实现](http://blog.csdn.net/javazejian/article/details/53375004)