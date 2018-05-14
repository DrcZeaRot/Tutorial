### HashMap常量

> bucket（桶）  和 bin（箱子）

* 约定前面的数组结构的每一个单元格称为桶(Hash碰撞，相同的hash放到同一个桶里)
* 约定桶后面跟随的每一个数据称为箱子(同一个桶里，equals不同的，放在不同的箱子里)

> size（个数）

size表示HashMap中存放K-V映射对的数量（为链表和树中的KV的总和）。

> capacity（容量）

* capacity就是指HashMap中桶的数量。默认值为16。一般第一次扩容时会扩容到64；
* 之后会扩容到当前的2倍，容量都是2的幂。

> loadFactor（装载因子）

* 装载因子用来衡量HashMap满的程度。loadFactor的默认值为0.75f。
* 计算HashMap的实时装载因子的方法为：size/capacity，而不是占用桶的数量去除以capacity。

> threshold(临界值)

* threshold表示当HashMap的元素的个数大于threshold时会执行resize操作。
* threshold=capacity*loadFactor

#### 默认参数

1. 默认容量
    ```
    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    ```
    * 默认初始化的容量为16，必须是2的幂。

2. 最大容量
    ```
    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;
    ```
    * 最大容量是2^30

3. 装载因子
    ```
    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    ```
    * 默认的装载因子是0.75

4. 由链表转换成树的阈值TREEIFY_THRESHOLD
    ```
    /**
     * The bin count threshold for using a tree rather than list for a
     * bin.  Bins are converted to trees when adding an element to a
     * bin with at least this many nodes. The value must be greater
     * than 2 and should be at least 8 to mesh with assumptions in
     * tree removal about conversion back to plain bins upon
     * shrinkage.
     */
    static final int TREEIFY_THRESHOLD = 8;
    ```
    * 一个桶中bin（箱子）的存储方式由链表转换成树的阈值。
    * 即当桶中bin的数量超过TREEIFY_THRESHOLD时使用树来代替链表。
    * 默认值是8

5. 由树转换成链表的阈值UNTREEIFY_THRESHOLD
    ```
    /**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     */
    static final int UNTREEIFY_THRESHOLD = 6;
    ```
    * 一个桶中bin（箱子）的存储方式由树转换成链表的阈值。
    * 当执行resize操作时，当桶中bin的数量少于UNTREEIFY_THRESHOLD时使用链表来代替树。
    * 默认值是6

6. 表扩容的阈值MIN_TREEIFY_CAPACITY
    ```
    /**
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
     * between resizing and treeification thresholds.
     */
    static final int MIN_TREEIFY_CAPACITY = 64;
    ```
    * 当桶中的bin被树化时，最小的hash表容量。
    1. 当HashMap中箱子的数量大于 TREEIFY_THRESHOLD ，小于 MIN_TREEIFY_CAPACITY 时，会执行resize扩容操作；
    2. 当HashMap中箱子的数量大于 MIN_TREEIFY_CAPACITY 时， 如果某个桶中的箱子数量大于8会被树化；
    3. 当HashMap中箱子数量 size  * 0.75 大于 threshold 的时候，就会扩容；
    4. threshold默认初始值 是 64，  默认装载因子是0.75；




