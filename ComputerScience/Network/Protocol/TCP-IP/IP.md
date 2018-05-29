### IP

1. 按层次分IP(Internet Protocol，网际协议)位于网络层
2. 几乎所有使用网络的系统都会用到IP协议
3. IP协议的作用：把各种数据包传送给对方
4. 要保证确实传送到对方，需要满足各类条件
    * 其中两个重要条件是：
    1. IP地址
        * IP地址指明了节点被分配到的地址
    2. MAC地址(Media Access Control Address)
        * MAC地址是指网卡所属的固定地址
    3. IP地址和MAC地址可以进行配对；IP地址可变换，MAC地址基本不会改变。
    4. 使用ARP(Address Resolution Protocol，地址转换协议)
        * 根据通信方的IP地址，就可以反查出对应的MAC地址
