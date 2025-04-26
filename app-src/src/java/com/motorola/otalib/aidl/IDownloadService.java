package com.motorola.otalib.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.motorola.otalib.aidl.IDownloadServiceCallback;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface IDownloadService extends IInterface {
    public static final String DESCRIPTOR = "com.motorola.otalib.aidl.IDownloadService";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class Default implements IDownloadService {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // com.motorola.otalib.aidl.IDownloadService
        public boolean downloadRequest(String str, String str2) throws RemoteException {
            return false;
        }

        @Override // com.motorola.otalib.aidl.IDownloadService
        public boolean registerCallback(String str, IDownloadServiceCallback iDownloadServiceCallback) throws RemoteException {
            return false;
        }

        @Override // com.motorola.otalib.aidl.IDownloadService
        public void unregisterCallback(String str) throws RemoteException {
        }
    }

    boolean downloadRequest(String str, String str2) throws RemoteException;

    boolean registerCallback(String str, IDownloadServiceCallback iDownloadServiceCallback) throws RemoteException;

    void unregisterCallback(String str) throws RemoteException;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static abstract class Stub extends Binder implements IDownloadService {
        static final int TRANSACTION_downloadRequest = 1;
        static final int TRANSACTION_registerCallback = 2;
        static final int TRANSACTION_unregisterCallback = 3;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IDownloadService.DESCRIPTOR);
        }

        public static IDownloadService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IDownloadService.DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof IDownloadService)) {
                return (IDownloadService) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(IDownloadService.DESCRIPTOR);
            }
            if (i == 1598968902) {
                parcel2.writeString(IDownloadService.DESCRIPTOR);
                return true;
            }
            if (i == 1) {
                boolean downloadRequest = downloadRequest(parcel.readString(), parcel.readString());
                parcel2.writeNoException();
                parcel2.writeInt(downloadRequest ? 1 : 0);
            } else if (i == 2) {
                boolean registerCallback = registerCallback(parcel.readString(), IDownloadServiceCallback.Stub.asInterface(parcel.readStrongBinder()));
                parcel2.writeNoException();
                parcel2.writeInt(registerCallback ? 1 : 0);
            } else if (i == 3) {
                unregisterCallback(parcel.readString());
                parcel2.writeNoException();
            } else {
                return super.onTransact(i, parcel, parcel2, i2);
            }
            return true;
        }

        /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
        private static class Proxy implements IDownloadService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IDownloadService.DESCRIPTOR;
            }

            @Override // com.motorola.otalib.aidl.IDownloadService
            public boolean downloadRequest(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadService.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadService
            public boolean registerCallback(String str, IDownloadServiceCallback iDownloadServiceCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadService.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStrongInterface(iDownloadServiceCallback);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IDownloadService
            public void unregisterCallback(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IDownloadService.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
