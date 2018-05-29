### 应用层

* 应用层决定了向用户提供应用服务时通信的活动，负责处理特定的应用程序细节
* 几乎各种不同的TCP/IP实现，都会提供下面这些通用的应用程序：
    1. FTP(File Transfer Protocol）是文件传输协议，一般上传下载用FTP服务，数据端口是20H，控制端口是21H。
    1. Telnet服务是用户远程登录服务，使用23H端口，使用明码传送，保密性差、简单方便。
    1. DNS(Domain Name Service）是域名解析服务，提供域名到IP地址之间的转换，使用端口53。
    1. SMTP(Simple Mail Transfer Protocol）是简单邮件传输协议，用来控制信件的发送、中转，使用端口25。
    1. NFS（Network File System）是网络文件系统，用于网络中不同主机间的文件共享。
    1. HTTP(Hypertext Transfer Protocol）是超文本传输协议，用于实现互联网中的WWW服务，使用端口80。