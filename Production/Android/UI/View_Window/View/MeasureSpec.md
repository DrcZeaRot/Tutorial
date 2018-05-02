### MeasureSpec

```
测量过程中，系统会将View的LayoutParams根据父容器锁施加的规则，转换成对应的MeasureSpec。
再根据这个MeasureSpec，测量出View的宽、高。
```

```
MeasureSpec代表一个32位的int值，通过位运算获取Mode和Size：
高2位代表SpecMode，指测量模式；
低30位代表SpecSize，指在某种测量模式下的规格大小。
```

##### SpecMode&SpecSize

SpecMode：
1. UNSPECIFIED：不定式
    * 父容器不对子View有任何限制，要多大给多大
    * 此情况一般用于系统内部，表示一种测量的状态
2. EXACTLY：精准模式
    * 父容器已经检测出View所需的精确大小。
    * 此时，View的最终大小就是SpecSize的值
    * MATCH_PARENT和具体数值，都对应这个Flag
3. AT_MOST：最大模式
    * 父容器指定了一个可用大小SpecSize
    * View的大小不能大于这个值(具体什么值，看不同View的具体实现)
    * WRAP_CONTENT对应这个Flag

SpecSize单纯只是一个数值。

##### MeasureSpec和LayoutParams的关系

观察源码，总结关系如下：
```
横轴：父容器的SpecMode
纵轴：子View的宽高
```

| childLayoutParams/parentSpecMode | EXACTLY | AT_MOST | UNSPECIFIED |
|:-:| :-:|:-:|:-:|
| dp/px | EXACTLY/childSize | EXACTLY/childSize | EXACTLY/childSize |
| match_parent | EXACTLY/parentSize | AT_MOST/parentSize | UNSPECIFIED/0 |
| wrap_content | AT_MOST/parentSize | AT_MOST/parentSize | UNSPECIFIED/0 |

简析：
1. 具体宽高
    * 无论父容器的SpecMode是什么
    * 子View的SpecMode都是EXACTLY，并且大小遵循params中的大小
2. match_parent
    * 父精准，则子精准、且大小为父的剩余空间
    * 父最大，则子最大、且大小不会超过父的剩余空间
3. wrap_content
    * 父精准或最大，子都是最大，且大小不超过父的剩余空间
4. UNSPECIFIED情况不需要特殊关注