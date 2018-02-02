### 队列

>队列同样是一种特殊的线性表，其插入和删除的操作分别在表的两端进行，队列的特点就是先进先出(First In First Out)。我们把向队列中插入元素的过程称为入队(Enqueue)，删除元素的过程称为出队(Dequeue)并把允许入队的一端称为队尾，允许出的的一端称为队头，没有任何元素的队列则称为空队。

#### 常见实现

##### 顺序队列
* 如果使用顺序表实现，在执行出队操作时，会对整个表进行移动，
时间复杂度为O(N)
* 通常实现为循环队列，增加上标front和下标rear，出队操作只需要改变front和rear，
无需移动元素，时间复杂度为O(1)

循环顺序队列设计思路：

    //其中front、rear的下标的取值范围是0~size-1，不会造成假溢出。
    front=(front+1)%size;//队头下标
    rear=(rear+1)%size;

> 1. front为队头元素的下标，rear则指向下一个入队元素的下标
> 2. 当front=rear时，我们约定队列为空。
> 3. 出队操作改变front下标指向，入队操作改变rear下标指向，size代表队列容量。
> 4. 约定队列满的条件为front=(rear+1)%size,注意此时队列中仍有一个空的位置，此处留一个空位主要用于避免与队列空的条件front=rear相同。
> 5. 队列内部的数组可扩容，并按照原来队列的次序复制元素数组

[循环顺序队列添加/删除](SequenceQueue.png)

[顺序队列示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/datastructure/queue/SequenceQueue.kt)
##### 链式队列
* 采用链表实现，单向/双向无所谓
* 拥有上标front和下标rear

设计思路：
> 1. 分别设置front和rear指向队头结点和队尾结点，使用单链表的头尾访问时间复杂度为O(1)。
> 2. 设置初始化空队列，使用front=rear=null，并且约定条件front==null&&rear==null成立时，队列为空。
> 3. 出队操作时，若队列不为空获取队头结点元素，并删除队头结点元素，更新front指针的指向为front=front.next
> 4. 入队操作时，使插入元素的结点在rear之后并更新rear指针指向新插入元素。
> 5. 当第一个元素入队或者最后一个元素出队时，同时更新front指针和rear指针的指向。

[链式队列示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/datastructure/queue/SingleLinkedQueue.kt)

##### 参考
[java数据结构与算法之（Queue）队列设计与实现](http://blog.csdn.net/javazejian/article/details/53375004)