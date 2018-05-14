### LinkedHashMap

> LinkedHashMap继承了HashMap，基于链表实现。

```
构建LinkedHashMap实例时，通过accessOrder参数
控制for each循环时，拿到数据的顺序：true为LRU顺序、false为插入顺序
```

#### 效果示例
```
val map = LinkedHashMap<String, String>(8, 0.75f, true/false)
            .apply {
                put("1", "A")
                put("2", "B")
                put("3", "C")
                put("4", "D")
            }
    map.get("3")
    map.get("2")
    map.put("5","E")

    map.forEach(BiConsumer { t, u -> "$t - $u".logW() })

false情况：
        1 - A
        2 - B
        3 - C
        4 - D
        5 - E
true情况：最近一次被get过的元素，会被放到队尾
        1 - A
        4 - D
        3 - C
        2 - B
        5 - E
```

#### 插入与获取

> LinkedHashMap::put方法直接调用HashMap::put，会调用final的HashMap::putVal

```
final V HashMap::putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        //newNode创建新节点，LinkedHashMap有不同实现。
        tab[i] = newNode(hash, key, value, null);
    else {
        ...
        if (e != null) { // existing mapping for key
            ...
            afterNodeAccess(e);//HashMap是空实现，给LinkedHashMap实现用
            return oldValue;
        }
    }
    ...
    afterNodeInsertion(evict);//HashMap是空实现，给LinkedHashMap实现用
    return null;
}
```

> LinkedHashMap::get有自己的实现

```
public V get(Object key) {
    Node<K,V> e;
    if ((e = getNode(hash(key), key)) == null)
        return null;
    if (accessOrder)
        afterNodeAccess(e);
    return e.value;
}
```

#### 重写方法

1. 创建新节点
    ```
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
        LinkedHashMap.Entry<K,V> p =
            new LinkedHashMap.Entry<K,V>(hash, key, value, e);
        linkNodeLast(p);
        return p;
    }
    //将新节点放到链表尾部
    private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
        LinkedHashMap.Entry<K,V> last = tail;
        tail = p;
        if (last == null)
            head = p;
        else {
            p.before = last;
            last.after = p;
        }
    }
    ```
2. 访问节点后：
    ```
    void afterNodeAccess(Node<K,V> e) { // move node to last
        LinkedHashMap.Entry<K,V> last;
        if (accessOrder && (last = tail) != e) {
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            p.after = null;
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;
        }
    }
    ```
    * accessOrder决定此方法是否产生效果
    * 效果是：将访问的节点，放到链表尾部；
    * 会造成一直没有用过的元素保持在链表头部，可以实现LRU效果(移除最近最少使用)。