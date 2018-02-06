### 二叉树删除节点

##### 二叉搜索树删除节点

>相比其他的树操作，删除稍微复杂一些，主要分为以下几种情况：

* 被删除节点是叶节点(没有子节点)
* 被删除节点只有一个子节点
* 被删除节点有两个子节点

##### 没有子节点
1. 被删除节点为root => rootNode = null
2. 被删除的是parent左节点 => parent.leftChild = null
3. 被删除的是parent右节点 => parent.rightChild = null

[没有子节点](../img/BinaryTreeRemoveNoChild.png)
##### 一个子节点
* 只有左节点
    1. 被删除节点为root => rootNode = currentNode.leftChild
    2. 被删除的是parent左节点 => parent.leftChild = currentNode.leftChild
    3. 被删除的是parent右节点 => parent.rightChild = currentNode.leftChild
* 只有右节点
    1. 被删除节点为root => rootNode = currentNode.rightChild
    2. 被删除的是parent左节点 => parent.leftChild = currentNode.rightChild
    3. 被删除的是parent右节点 => parent.rightChild = currentNode.rightChild

[一个子节点](../img/BinaryTreeRemoveSingleChild.png)
##### 两个子节点
>这个情况是最为复杂的，需要保证二叉树相对平衡，主要操作是寻找[后继节点]()。

"后继节点"是即将出现在被删除节点位置的新节点，有如下特点
* 比被删除的节点大
* 在所有更大的节点中，又是最小的
* 简而言之：删除节点右子树中的最左子节点
* 窍门是：在右子树中不停寻找下一个左子节点，什么时候下一个左子节点是null，这个节点就是后继节点

[删除过程-右节点是后继节点](../img/BinaryTreeRemoveDoubleChildRight.png)

[删除过程-左节点是后继节点](../img/BinaryTreeRemoveDoubleChildLeft.png)

