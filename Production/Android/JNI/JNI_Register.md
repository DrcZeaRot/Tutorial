### 静态注册&动态注册

##### 静态注册

1. Java调用C/C++
    1. Java代码中，声明native方法：
        ```
        public class JniTester{
            //调用加载LIB库
            static{ System.loadLibrary("ndk_test"); }
            public native void staticJni();
        }
        ```
    2. C/C++中编写对应签名方法：
        ```
        JNIEXPORT void JNICALL Java_com_example_JniTester_staticJni(JNIEnv * env, jobject clazz)
        ```
        * 具体各个关键字、参数含义，见[JNI基础扫盲](JNI_Intro.md)-C/C++中JNI方法签名简介
2. C/C++调用Java
    1.  随便你写一个等待调用的方法
        ```
        public void callMeInNative(String msg){
            ......
        }
        ```
    2. 实现本地方法(C++版本)
        ```
        void callJava(JNIEnv *env, jobject obj){
            //先找到class
            jclass tester = env->FindClass("com/example/JniTester");
            //获取需要调用的方法id
            jmethodID methodId = env->GetMethodID(tester,"callMeInNative","()V")
            //调用方法：
            env->CallVoidMethod(obj,methodID,msg)
            //如果是C版本会是这样：(*env)-> CallVoidMethod(env,obj,methodID,msg)
        }
        ```
        * GetMethodID参数：
            1. 目标class
            2. 方法名
            3. 方法参数签名，见：[JVM字段表集合](../../../ProgrammingLanguage/Java/JVM/ClassFileStructure/CFS_FieldList.md) - 描述符
        * Call???Method：
            1. 根据返回类型不同会有多个类似方法
            2. obj为调用方法的对象
            3. methodID为调用方法的jmethodID签名
            4. 后续为java方法需要的参数
##### 动态注册

Java调用C:
1. 声明如下java的native方法：
    ```
    public class JniTester{
        //调用加载LIB库
        static{ System.loadLibrary("ndk_test"); }
        public native String dynamicJniGetString();
    }
    ```
2. C++中进行动态注册：
    1. 实现C++版本的native方法：
        ```
        //可以随意定义方法名
        jstring getString(){
            JNIEnv *env = NULL;
            //g_jvm为JavaVM指针，在JNI_OnLoad()方法中获得此指针。这句好像并不必要，不要在意细节(目前不是太懂语法)。
            g_jvm->AttachCurrentThread(&env, NULL);
            return env->NewStringUTF("This is a Natvie String!");
        }
        ```
    2. 定义JNINativeMethod数组，声明需要注册的方法：
        1. JNINativeMethod结构体:
            ```
            typedef struct {
                const char* name;       //对应的java方法名
                const char* signature;  //方法描述符
                void*       fnPtr;      //native方法指针
            } JNINativeMethod;
            ```
        2. 数组：这里定义所有需要注册的方法
            ```
            static JNINativeMethod methods[] = {
                    {"dynamicJniGetString", "()Ljava/lang/String;", (void*)getString}//这是一个JNINativeMethod对象。
            };
            ```
    3. JNI_OnLoad()方法：
        ```
        JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved){
            //OnLoad方法是没有JNIEnv参数的，需要通过vm获取。
            JNIEnv* env;
            if (JNI_OK != vm->GetEnv(reinterpret_cast<void**> (&env),JNI_VERSION_1_4)) {
                LOGW("JNI_OnLoad could not get JNI env");
                return JNI_ERR;
            }

            g_jvm = vm; //用于后面获取JNIEnv
            jclass clazz = env->FindClass("com/example/JniTester");  //获取Java类

            //注册此类中的Native方法
            if (env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof((methods)[0])) < 0) {
                LOGW("RegisterNatives error");
                return JNI_ERR;
            }

            return JNI_VERSION_1_4;
        }
        ```
        * 当本地库被加载时VM调用JNI_OnLoad（例如，System.LoadLibrary）。JNI_OnLoad必须返回由本地库所需的JNI版本。
        * 为了使用任何新的JNI函数，一个本地库必须导出JNI_OnLoad函数并返回JNI_VERSION_1_2或更高的版本。
        * 如果本地库不导出JNI_OnLoad功能，VM假定库只需要JNI_VERSION_1_1版本。
        * 如果虚拟机不认JNI_OnLoad返回的版本号，本地库不能加载。
    4. RegisterNatives(JNI_OnLoad中调用)：
        * 完整签名：
            ```
            jint RegisterNatives(jclass clazz, const JNINativeMethod* methods, jint nMethods);
            ```
        * 这是JNIEnv提供的注册本地方法
        * 参数：
            1. clazz：方法对应的class
            2. methods:对应的方法数组指针
            3. nMethods：有几个方法
            4. 返回值：注册成功返回JNI_OK
##### 参考

[Android NDK JNI动态注册本地方法](https://blog.csdn.net/venusic/article/details/52487558)

[安卓JNI开发之动态注册native方法](https://www.jianshu.com/p/67019062774b)

[Android的JNI_OnLoad简介与应用](https://blog.csdn.net/fireroll/article/details/50102009)