### 插入操作

树化条件：
1. 如果在创建HashMap实例时没有给定capacity、loadFactor则默认值分别是16和0.75。
2. 当好多bin被映射到同一个桶时
    1. 桶中bin的数量小于TREEIFY_THRESHOLD(树化阈值)，不会转化成树形结构存储；
    2. 桶中bin的数量大于了，TREEIFY_THRESHOLD
        1. 如果capacity小于MIN_TREEIFY_CAPACITY，则依然使用链表结构进行存储，此时会对HashMap进行扩容；
        2. 如果capacity大于MIN_TREEIFY_CAPACITY ，则会进行树化。

插入细节：
1. 桶里的bin可能以链表/树的两种结构存储
    1. 链表采取正常的尾插法，并根据bin的数量决定是否树化
        * ConcurrentHashMap由于Node的next为final，采取头插法(JDK1.7版本如此)
2. 如果Key已存在，则修改并返回旧值；否则执行插入，并返回null


插入流程：
1. 通过hash值得到所在bucket的下标，如果为null，表示没有发生碰撞，则直接put
2. 如果发生了碰撞，则解决发生碰撞的实现方式：链表还是树。
3. 如果能够找到该key的结点，则执行更新操作，无需对modCount增1。
4. 如果没有找到该key的结点，则执行插入操作，需要对modCount增1。
5. 在执行插入操作时，如果bucket中bin的数量超过TREEIFY_THRESHOLD，则要树化。
6. 在执行插入操作之后，如果数组size超过了threshold，这要扩容
