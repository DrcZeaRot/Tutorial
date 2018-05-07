### Activity启动模式&任务栈

4种启动模式：
1. standard：标准模式,默认的启动模式，典型的多实例实现。
    * 此模式的Activity运行在它被启动的任务栈中
    * 如：A启动B(B是标准模式)，B会进入A的任务栈中
    * 使用ApplicationContext启动Activity时
        * AppContext没有任务栈，导致无法正常启动
        * 添加FLAG_ACTIVITY_NEW_TASK，可以正常启动(此时，新的Activity是singleTask模式)
2. singleTop：栈顶复用模式
    * 新Activity如果以位于栈顶，则Activity不会被重新创建
    * onNewIntent方法回调
3. singleTask：栈内复用模式
    * 只要Activity在某一个任务栈中存在，多次启动它，都不会重新创建实例
        * onNewIntent方法回调
    * singleTask的A请求启动：
        1. 系统先寻找：是否存在A想要的任务栈
        2. 如果不存在，就创建任务栈，然后创建A的实例并放到栈中
        3. 如果存在：
            1. 该栈中如果有A的实例，就将A调到栈顶，回调onNewIntent方法
            2. 该栈中没有A的实例，就创建A的实例，再压入栈中
    * 举例：
        1. 栈S1中有ABC，3个Activity，此时D以singleTask模式请求启动，D需要栈S2
            * 由于S2与D都不存在，系统先创建S2，在创建D，然后将D放入S2
        2. 例1中，D如果也需要栈S1
            * 由于S1以存在，但D不存在，系统创建D，然后将D放入S1
        3. D需求栈S1，S1中有ADBC，4个Activity
            * 此时D不会被重新创建，会被调到S1栈顶，并回调onNewIntent方法
            * singleTask自带clearTop效果，所以D上方的BC被clear
            * 最终S1中情况为：AD，2个Activity
4. singleInstance：单实例模式
    * 加强的singleTask模式
    * 除了具有singleTask所有特性，还有：此模式的Activity只能单独位于一个任务栈中
    * 这类的Activity启动后，系统会为它创建一个新的任务栈，此Activity单独运行在这个栈中
    * 由于栈复用的特性，只要这个独特的任务栈没有销毁、这个Activity没有销毁，后续的启动都不会重新创建新的Activity

小示例：
* 假设有2个栈，前台栈S1中有AB，后台栈S2中有CD(CD均为singleTask)
    1. 前台请求启动D：则后台栈S2中的任务，被整个切换到前台
        * 前台栈S1变为ABCD
        * 弹栈一次变为ABC，再弹栈变为AB
    2. 前台请求启动C：前台栈S1变为ABC(D被clearTop掉)

##### 任务栈

* TaskAffinity(任务相关性)
    * 此参数标识了一个Activity需要的任务栈名字
    * 默认，所有Activity所需的任务栈，名字都是应用的包名
    * 但每个Activity都可以指定单独的TaskAffinity(不能与包名相同，否则等于没有指定)
    * 此属性主要与singleTask模式，还有allowTaskReparenting属性，配对使用
1. TaskAffinity与singleTask配合：
    * 带启动的Activity，会在TaskAffinity这个名称的任务栈中运行
2. TaskAffinity与allowTaskReparenting配合：
    * 应用A启动应用B的某个Activity之后，
        * 此Activity的allowTaskReparenting为true
        * 当B被启动，此Activity直接从A的任务栈，转移到B的任务栈中
    * 具体示例：
        * 两个应用A、B
        * A启动B中的Activity C，然后按Home回桌面
        * 从桌面点击B的图标，启动B
        * 此时，B的主Activity不会启动，而是启动C。
        * C会从A的任务栈，转移到B的任务栈中。
    * 可以这样理解：
        * A启动C时，C只能运行在A的任务栈中(因为没有其他相关的栈)
        * 但C是属于B应用的，所以正常情况下，C需求的任务栈肯定与A的任务栈不同(包名)
        * 所以，一旦B被启动，B创建自己的任务栈
        * 此时，系统发现C原本想要的任务栈，终于被创建了
        * 就把C从A的任务栈中转移过来




