### SparseArray

```
通常用来代替HashMap<Integer,V>，省去int的拆装箱过程(Kotlin的IntArray等也是这样)。
内部有两个数组：int[] mKeys，Object[] mValues。
```

* 占用内存比HashMap小很多
* 插入、查找之类的操作都根据二分查找<O(lgN)>
    * 数据量大了之后，二分法效率极具下降，将远远慢于Hash表<最优<O(1)>，最差<O(n)>，JDK8中引入红黑树，最差会好一些>
* 适用于：千级以内的数据量、Key为int的映射
* 更有SparseBooleanArray、SparseIntArray、SparseLongArray来解决Value也需要装箱的情况。
