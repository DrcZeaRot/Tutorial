### HashMap

简单描述：
1. HashMap基于：数组、链表；JDK8时，还有红黑树
2. HashMap允许：key、value都可以为null
3. HashMap线程不安全；可以使用同步措施实现线程安全，但高并发应使用ConcurrentHashMap；不要使用HashTable
4. HashMap默认参数：容量16、装载因子0.75；

#### 实现简述

* HashMap的实现使用了数组、链表、红黑树
* 数组的每个元素，称之为桶，桶里存放箱子
* 桶的数据结构有2种可能：链表、红黑树
* HashMap使用key的hashCode来寻找存储位置
* 不同的key可能具有相同的hashCode，这时候就出现哈希冲突了，也叫做哈希碰撞，为了解决哈希冲突
* 常见办法有开放地址方法、链地址方法，HashMap的实现上选取了链地址方法
* 也就是将哈希值一样的，但是equals是false的，entry保存在同一个数组项里面
* 每一个数组项当做一个桶，桶里面装的entry的key的hashCode是一样的

#### 细节描述

* [关键定义](HashMap_Constant.md)