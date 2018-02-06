package com.xcstasy.tutorial.algorithm.sort

class MergeSort : IntSort {
    override fun execute(source: IntArray) {
        mergeSort(source)
//        recMergeSort(source, 0, source.size - 1, IntArray(source.size))//在排序前，先建好一个长度等于原数组长度的临时数组，避免递归中频繁开辟空间
    }

    private fun mergeSort(source: IntArray) {
        val size = source.size
        val temp = IntArray(size)

        var leftStart: Int
        var leftEnd: Int
        var rightStart: Int
        var rightEnd: Int

        var leftIndex: Int
        var rightIndex: Int
        var tempIndex: Int

        var span = 1    //最小分解单元大小
        while (span < size) {//合并后的大小还要在数据源大小之内
            tempIndex = 0
            leftStart = 0
            while (leftStart + span < size) {
                leftEnd = leftStart + span - 1
                rightStart = leftStart + span
                rightEnd = (rightStart + span - 1).let { if (it <= size - 1) it else size - 1 }

                leftIndex = leftStart
                rightIndex = rightStart

                while (leftIndex <= leftEnd && rightIndex <= rightEnd) {
                    //交替比较左右两个序列同一个index的数据大小，较小的先放进temp中
                    if (source[leftIndex] <= source[rightIndex]) {
                        temp[tempIndex++] = source[leftIndex++]
                    } else {
                        temp[tempIndex++] = source[rightIndex++]
                    }
                }
                while (leftIndex <= leftEnd) {//将左边剩余元素填充进temp中
                    temp[tempIndex++] = source[leftIndex++]
                }
                while (rightIndex <= rightEnd) {//将右序列剩余元素填充进temp中
                    temp[tempIndex++] = source[rightIndex++]
                }
                leftStart = rightEnd + 1  //前往下一个span
            }
            for (m in leftStart until size) {//剩余元素拷贝到temp中
                temp[tempIndex++] = source[m]
            }
            for (n in 0 until size) {
                source[n] = temp[n]
            }
            span *= 2   //合并
        }
    }

    /**
     * 递归并归排序
     */
    private fun recMergeSort(source: IntArray, left: Int, right: Int, temp: IntArray) {
        if (left < right) {
            val mid = (left + right) / 2
            recMergeSort(source, left, mid, temp)//左边归并排序，使得左子序列有序
            recMergeSort(source, mid + 1, right, temp)//右边归并排序，使得右子序列有序
            merge(source, left, mid, right, temp)//将两个有序子数组合并操作
        }
    }

    private fun merge(source: IntArray, left: Int, mid: Int, right: Int, temp: IntArray) {
        var i = left    //左序列index
        var j = mid + 1 //右序列index
        var t = 0       //临时序列index
        while (i <= mid && j <= right) {
            //交替比较左右两个序列同一个index的数据大小，较小的先放进temp中
            if (source[i] <= source[j]) {
                temp[t++] = source[i++]
            } else {
                temp[t++] = source[j++]
            }
        }
        while (i <= mid) {//将左边剩余元素填充进temp中
            temp[t++] = source[i++]
        }
        while (j <= right) {//将右序列剩余元素填充进temp中
            temp[t++] = source[j++]
        }
        //将temp中的元素全部拷贝到原数组中
        t = 0
        var newLeft = left
        while (newLeft <= right) {
            source[newLeft++] = temp[t++]
        }
    }
}