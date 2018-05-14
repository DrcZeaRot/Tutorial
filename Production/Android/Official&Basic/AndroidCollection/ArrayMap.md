### ArrayMap

```
内部两个数组：
1. int[] mHashes，保存key的hash值
2. Object[] mArrays，长度是mHashes的两倍，key和value相邻保存在这个数组中
```
0. 查找原理
    1. 插入时通过二分法找到对应key的hash值在mHashes的index(由于是二分法，并且相同的hash都相邻，可能找到某一个)，
    2. 如果该hash值的key与当前key相同，则寻找正确，直接插入
    3. 如果该index上hash值相同而key不同，就+1，向后找，直到找到hash相同、key一样
        * 如果上述寻找中，hash值不同，则不再向后找、转而向第一个index的前方找
        * 如果向前找到也是找到hash值不同也没发现正确的，则返回之前向后找的最后一个index+1再取反
    4. 总而言之，这是一种线性探测，在hash碰撞时效率很低
1. 查找效率：
    1. HashMap找桶是O(1)，碰撞之后，桶内找箱子JDK7是链表、JDK8是树；箱子变长，效率变低，但树的效率还是很高的。
    2. ArrayMap是二分法O(logN)，长度增加一倍就多一次查找
    * 容量大的时候使用HashMap
2. 扩容数量
    1. HashMap默认初始16个，扩容翻倍，申请双倍空间
    2. ArrayMap扩容时：如果size大于8，申请size*1.5，大于4小于8则申请8个，小于4则申请4个。
    * 容量大时候使用HashMap
3. 扩容效率
    1. HashMap扩容需要重新进行hash，计算数组成员的新位置
    2. ArrayMap使用System.arrayCopy，效率较高
4. 内存消耗：
    1. ArrayMap采用一种独特方式，能重复利用因为扩容而遗留下的数组空间，方便下一个ArrayMap使用
    2. HashMap没有相关优化设计。
    * 数据量较小时，ArrayMap在内存方面完胜
