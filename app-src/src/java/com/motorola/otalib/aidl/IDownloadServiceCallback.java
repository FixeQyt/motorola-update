package com.motorola.otalib.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface IDownloadServiceCallback extends IInterface {
    public static final String DESCRIPTOR = "com.motorola.otalib.aidl.IDownloadServiceCallback";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class Default implements IDownloadServiceCallback {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void dlResponse(String str) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void errorCode(String str, int i, boolean z) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void exception(String str, String str2) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void failed(String str, int i, String str2, String str3) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void finished(String str) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void initFailed(String str, String str2, String str3) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void progress(String str) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void suspended(String str, boolean z) throws RemoteException {
        }
    }

    void dlResponse(String str) throws RemoteException;

    void errorCode(String str, int i, boolean z) throws RemoteException;

    void exception(String str, String str2) throws RemoteException;

    void failed(String str, int i, String str2, String str3) throws RemoteException;

    void finished(String str) throws RemoteException;

    void initFailed(String str, String str2, String str3) throws RemoteException;

    void progress(String str) throws RemoteException;

    void suspended(String str, boolean z) throws RemoteException;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static abstract class Stub extends Binder implements IDownloadServiceCallback {
        static final int TRANSACTION_dlResponse = 1;
        static final int TRANSACTION_errorCode = 6;
        static final int TRANSACTION_exception = 5;
        static final int TRANSACTION_failed = 3;
        static final int TRANSACTION_finished = 4;
        static final int TRANSACTION_initFailed = 8;
        static final int TRANSACTION_progress = 2;
        static final int TRANSACTION_suspended = 7;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IDownloadServiceCallback.DESCRIPTOR);
        }

        public static IDownloadServiceCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IDownloadServiceCallback.DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof IDownloadServiceCallback)) {
                return (IDownloadServiceCallback) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(IDownloadServiceCallback.DESCRIPTOR);
            }
            if (i == 1598968902) {
                parcel2.writeString(IDownloadServiceCallback.DESCRIPTOR);
                return true;
            }
            switch (i) {
                case 1:
                    dlResponse(parcel.readString());
                    parcel2.writeNoException();
                    break;
                case 2:
                    progress(parcel.readString());
                    parcel2.writeNoException();
                    break;
                case 3:
                    failed(parcel.readString(), parcel.readInt(), parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    break;
                case 4:
                    finished(parcel.readString());
                    parcel2.writeNoException();
                    break;
                case 5:
                    exception(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    break;
                case 6:
                    errorCode(parcel.readString(), parcel.readInt(), parcel.readInt() != 0);
                    parcel2.writeNoException();
                    break;
                case 7:
                    suspended(parcel.readString(), parcel.readInt() != 0);
                    parcel2.writeNoException();
                    break;
                case 8:
                    initFailed(parcel.readString(), parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    break;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
            return true;
        }

        /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
        private static class Proxy implements IDownloadServiceCallback {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IDownloadServiceCallback.DESCRIPTOR;
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void dlResponse(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void progress(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void failed(String str, int i, String str2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void finished(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void exception(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void errorCode(String str, int i, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeInt(z ? 1 : 0);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void suspended(String str, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(z ? 1 : 0);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
            public void initFailed(String str, String str2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadServiceCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
