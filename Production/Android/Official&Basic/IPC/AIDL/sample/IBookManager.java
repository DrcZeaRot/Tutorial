/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\workspace\\Studio\\Larva\\larva-sample\\src\\main\\aidl\\com\\xcstasy\\r\\larva\\sample\\aidl\\IBookManager.aidl
 */
package com.xcstasy.r.larva.sample.aidl;

public interface IBookManager extends android.os.IInterface {

    public java.util.List<com.xcstasy.r.larva.sample.aidl.Book> getBookList() throws android.os.RemoteException;

    public void addBook(com.xcstasy.r.larva.sample.aidl.Book book) throws android.os.RemoteException;

    /**
     * Local-side IPC implementation stub class.<br/>
     * 如果不夸进程，Service传递的IBookManager实现，就会是这个Stub类
     */
    public static abstract class Stub extends android.os.Binder implements com.xcstasy.r.larva.sample.aidl.IBookManager {

        /**
         * Binder的唯一标识，一般用当前Binder的类名表示。
         */
        private static final java.lang.String DESCRIPTOR = "com.xcstasy.r.larva.sample.aidl.IBookManager";

        /*
        这两个常量用于标识对应方法，在onTransact方法中进行case的区分。
         */
        static final int TRANSACTION_getBookList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_addBook = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.xcstasy.r.larva.sample.aidl.IBookManager interface,
         * generating a proxy if needed.
         * <p>
         *     将服务端的Binder对象，转化成客户端需求的AIDL接口对象。<br/>
         *     转化过程根据进程不同：<br/>
         *        1. 相同进程：返回服务端的Stub <br/>
         *        2. 不同进程：返回系统封装后的Stub.Proxy <br/>
         * </p>
         *
         */
        public static com.xcstasy.r.larva.sample.aidl.IBookManager asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof com.xcstasy.r.larva.sample.aidl.IBookManager))) {
                return ((com.xcstasy.r.larva.sample.aidl.IBookManager) iin);
            }
            return new com.xcstasy.r.larva.sample.aidl.IBookManager.Stub.Proxy(obj);
        }

        /**
         * 返回当前Binder对象。
         */
        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        /**
         * 此方法运行在服务端中的Binder线程池中。<br/>
         * 当客户端发起跨进程请求时：远程请求通过系统底层封装后，交由此方法处理。(Stub.Proxy的实现跳转此方法)<br/>
         *
         * @param code  服务端通过code确定客户端请求的目标方法
         * @param data  从data中可以取出目标方法的参数(如果有入参)，用于执行方法
         * @param reply 执行完目标方法，向reply中写入返回值(如果有返回值)
         * @param flags
         * @return  如果返回false，客户端请求就失败。可以通过这个特性进行权限验证，保证不是随便一个进程都能调用服务。
         * @throws android.os.RemoteException
         */
        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_getBookList: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<com.xcstasy.r.larva.sample.aidl.Book> _result = this.getBookList();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                }
                case TRANSACTION_addBook: {
                    data.enforceInterface(DESCRIPTOR);
                    com.xcstasy.r.larva.sample.aidl.Book _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = com.xcstasy.r.larva.sample.aidl.Book.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.addBook(_arg0);
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        /**
         * 如果夸进程，Service传递的IBookManager的实现，就会是这个Stub.Proxy类
         */
        private static class Proxy implements com.xcstasy.r.larva.sample.aidl.IBookManager {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            /**
             * 这个方法运行在客户端
             */
            @Override
            public java.util.List<com.xcstasy.r.larva.sample.aidl.Book> getBookList() throws android.os.RemoteException {
                /**
                 * 声明需要的参数、返回值的载体：Parcel
                 */
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.util.List<com.xcstasy.r.larva.sample.aidl.Book> _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    /**
                     * transact是一次RPC(远程过程调用)，调用后会挂起当前线程<br/>
                     * 然后，服务端的Stub的onTransact方法会被回调
                     */
                    mRemote.transact(Stub.TRANSACTION_getBookList, _data, _reply, 0);
                    /**
                     * RPC过程返回后，当前线程继续执行，从_reply中获取RPC过程的返回结果
                     */
                    _reply.readException();
                    _result = _reply.createTypedArrayList(com.xcstasy.r.larva.sample.aidl.Book.CREATOR);
                } finally {
                    //释放资源
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            /**
             * 与上述有返回值的getBookList过程相同。
             */
            @Override
            public void addBook(com.xcstasy.r.larva.sample.aidl.Book book) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((book != null)) {
                        _data.writeInt(1);
                        book.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_addBook, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
