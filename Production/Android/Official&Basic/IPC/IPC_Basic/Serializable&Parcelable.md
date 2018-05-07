### Serializable&Parcelable

> Serializable&Parcelable，这两个接口可以完成对象的序列化过程。

#### Serializable

Serializable是Java提供的序列化接口：
* 它一个空接口，只要实现它，就可以自动实现默认的序列化过程
* 可以添加serialVersionUID来保证反序列化的安全性
* 使用ObjectStream对象流，进行对象的序列化/反序列化
    ```
    class User(val userId: Int, val userName: String, val isMale: Boolean) : Serializable

    fun main(args: Array<String>) {
        val user = User(0, "jake", true)
        ObjectOutputStream(File("cache.txt").outputStream()).use {
            it.writeObject(user)
        }
        ObjectInputStream(File("cache.txt").inputStream()).use {
            val newUser: User = it.readObject() as User
        }
    }
    ```
    * 上述代码实现了User类的序列化/反序列化
    * 从文件"cache.txt"中恢复的newUser与user的属性完全相同，只不过不是同一个实例
* serialVersionUID的用途：
    * 原则上：序列化后的数据，只有serialVersionUID和当前类的serialVersionUID相同，才能正常反序列化。
    * 工作原理：
        1. 序列化时，系统把当前类的serialVersionUID写入序列化文件中
        2. 反序列化时，系统去检测文件中的serialVersionUID是否与当前类一致。
            1. 一致说明版本相同，可以成功反序列化
            2. 不一致，可能是当前类已经产生了某些变化，反序列化可能会产生属性的不安全。
    * 对实现Serializable接口的类：
        * 尽量在修改类内容时，更新serialVersionUID
        * 保证不会有旧版本的文件，被反序列化为新版本的实例。
* 通过实现read/write方法，可以指定序列化/反序列化的细节

#### Parcelable

> Parcelable主要是繁琐一些，还好有强大的IDE跟Kotlin

与Serializable的区别：
* Serializable主要用于外存储，可以把对象序列化到SD卡中
    * 开销较大，过程中需要大量IO操作。
* Parcelable主要将对象序列化到内存中
    * 也可以像Serializable一样用于外存储、或进行网络传输，但比较复杂，推荐直接使用Serializable
    * Parcelable效率很高
