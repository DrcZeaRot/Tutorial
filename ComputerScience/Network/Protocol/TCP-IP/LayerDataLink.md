### 数据链路层(链路层、网络接口层)

1. 用来处理连接网络的硬件部分，包括：
    * 硬件上的范畴均在链路层的作用范围之内
    1. 控制操作系统、 硬件的设备驱动
    2. NIC(Network Interface Card，网络适配器，即网卡)
    3. 光纤等物理可见部分(还包括连接器等一切传输媒介)
2. 数据链路层是负责接收IP数据包并通过网络发送，或者从网络上接收物理帧，抽出IP数据包，交给IP层
3. 常见协议有：
    * Ethernet 802.3、Token Ring 802.5、X.25、Frame relay、HDLC、PPP ATM等。