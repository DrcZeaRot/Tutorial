package com.xcstasy.tutorial.designpattern.structure.proxy

import com.xcstasy.tutorial.util.logW

//================== 普通代理： 客户端只能访问代理角色，不能访问真实角色 =====================

//普通代理接口
interface IGamePlayer {
    fun login(userName: String, password: String)
    fun killBoss()
    fun levelUp()
}
//具体的实例
class GamePlayer(
        //要求player必须非null，即不能创建真实的角色、只能通过代理进行使用
        player: IGamePlayer, private val name: String
) : IGamePlayer {

    override fun login(userName: String, password: String) = "${name}登录成功".logW()

    override fun killBoss() = "${name}在打怪".logW()

    override fun levelUp() = "${name}升级了".logW()
}

//普通代理实现
class GamePlayerProxy(name: String) : IGamePlayer {

    private val gamePlayer: IGamePlayer = GamePlayer(this, name)

    override fun login(userName: String, password: String) = gamePlayer.login(userName, password)

    override fun killBoss() = gamePlayer.killBoss()

    override fun levelUp() = gamePlayer.levelUp()
}

//================== 强制代理： 强制代理的概念就是要从真实角色查找到代理角色， 不允许直接访问真实角色 =====================

//强制代理接口
interface IForceGamePlayer {
    fun login(userName: String, password: String)
    fun killBoss()
    fun levelUp()
    //真实角色GamePlayer可以指定一个自己的代理， 除了代理外谁都不能访问
    fun getProxy(): IForceGamePlayer
}

//具体的实例，只能通过真实角色getProxy()获取的代理，进行操作
class ForceGamePlayer(private val name: String) : IForceGamePlayer {

    private var proxy: IForceGamePlayer? = null

    override fun login(userName: String, password: String) =
            if (isProxy) {
                "${name}登陆成功".logW()
            } else {
                "请使用指定的代理访问".logW()
            }

    override fun killBoss() =
            if (isProxy) {
                "${name}在打怪".logW()
            } else {
                "请使用指定的代理访问".logW()
            }

    override fun levelUp() =
            if (isProxy) {
                "${name}升级了".logW()
            } else {
                "请使用指定的代理访问".logW()
            }

    override fun getProxy(): IForceGamePlayer = proxy ?: ForceGamePlayerProxy(this).apply { proxy = this }

    private val isProxy: Boolean
        get() = proxy == null
}
//强制代理实现
class ForceGamePlayerProxy(private val gamePlayer: IForceGamePlayer) : IForceGamePlayer {
    override fun login(userName: String, password: String) = gamePlayer.login(userName, password)
    override fun killBoss() = gamePlayer.killBoss()
    override fun levelUp() = gamePlayer.levelUp()
    override fun getProxy(): IForceGamePlayer = this
}


//================== 功能性代理： 代理类可以为真实角色预处理消息、 过滤消息、 消息转发、 事后处理消息等功能=====================

//功能性代理接口
interface IProxy {
    //计算费用
    fun count()
}
//功能性代理实现
class CountGamePlayerProxy(private val gamePlayer: IForceGamePlayer) : IForceGamePlayer, IProxy {

    override fun login(userName: String, password: String) = gamePlayer.login(userName, password)
    override fun killBoss() = gamePlayer.killBoss()
    override fun levelUp() {
        gamePlayer.levelUp()
        count()
    }

    override fun getProxy(): IForceGamePlayer = this
    override fun count() = "升级费用共$150".logW()
}