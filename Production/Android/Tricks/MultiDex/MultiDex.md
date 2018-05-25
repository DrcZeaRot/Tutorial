### MultiDex

基础知识： [DexClassLoader](DexClassLoader.md)

#### 基本原理
1. 除了第一个dex文件（即正常apk包唯一包含的Dex文件），其它dex文件都以资源的方式放在安装包中
    ```
    所以我们需要将其他dex文件并在Application的onCreate回调中注入到系统的ClassLoader。
    并且对于那些在注入之前已经引用到的类（以及它们所在的jar）,必须放入第一个Dex文件中。
    ```
2. PathClassLoader作为默认的类加载器，在打开应用程序的时候PathClassLoader就去加载指定的apk
    * (解压成dex，然后在优化成odex)，也就是第一个dex文件是PathClassLoader自动加载的
    * 所以，我们需要做的就是将其他的dex文件注入到这个PathClassLoader中去
3. 因为PathClassLoader和DexClassLoader的原理基本一致(都是BaseDexClassLoader的子类)
    * 从前面的分析来看，我们知道PathClassLoader里面的dex文件是放在一个Element数组里面，可以包含多个dex文件
    * 每个dex文件是一个Element，所以我们只需要将其他的dex文件放到这个数组中去就可以了
#### 实现思路
1. 通过反射获取PathClassLoader中的DexPathList中的Element数组（已加载了第一个dex包，由系统加载）
2. 通过反射获取DexClassLoader中的DexPathList中的Element数组（将第二个dex包加载进去）
3. 将两个Element数组合并之后，再将其赋值给PathClassLoader的Element数组
4. 官方的support.MultiDex就是这种实现

#### 参考

[MultiDex与热修复实现原理](https://blog.csdn.net/hp910315/article/details/51681710)

[Android中apk加固完善篇之内存加载dex方案实现原理(不落地方式加载)](https://blog.csdn.net/jiangwei0910410003/article/details/51557135)