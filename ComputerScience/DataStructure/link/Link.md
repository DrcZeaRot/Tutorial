### 链表

* 链表按节点结构分为：单向列表、双向列表。
* 按节点关系又分为：顺序列表、循环列表

1. 链表都由若干个"节点"组成。不同结构的链表，拥有不同的节点
2. 指针的访问只能通过头结点或者尾节点进行访问；
3. 当删除节点，要先处理好节点的pre和next指针的指向，再删除节点、回收内存资源；

##### 单向链表
>单向链表(单链表)是链表的一种，它由节点组成，每个节点只包含下一个节点的指针。

* [单链表添加](SingleLinkAdd.jpg)
* [单链表删除](SingleLinkRemove.jpg)

[单链表示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/datastructure/link/SingleLinkList.kt)

##### 双向链表

* [双向循环链表添加](DoubleLinkAdd.jpg)
* [双向循环链表删除](DoubleLinkRemove.jpg)

1. 双向链表(双链表)是链表的一种。
2. 和单链表一样，双链表也是由节点组成，它的每个数据结点中都有两个指针，分别指向直接后继和直接前驱。
3. 所以，从双向链表中的任意一个结点开始，都可以很方便地访问它的前驱结点和后继结点。
4. 一般我们都构造双向循环链表。

* [双向链表示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/datastructure/link/DoubleLinkList.kt)


##### 顺序列表
>通常只有一个Head节点

##### 循环列表
>拥有一个Head、一个Tail，或者最后一个节点与Head节点一直相互关联。

* [循环链表示例](../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/datastructure/link/CycleLinkList.kt)

##### 参考
[线性表--数组、单链表、双链表](https://wangkuiwu.github.io/2013/01/01/dlink/)