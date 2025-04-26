package com.motorola.otalib.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.motorola.otalib.aidl.IOtaLibServiceCallBack;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface IOtaLibService extends IInterface {
    public static final String DESCRIPTOR = "com.motorola.otalib.aidl.IOtaLibService";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class Default implements IOtaLibService {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public void checkForUpdate(String str) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public void downloadConfigFiles(String str) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public void onStatusUpdateBackToLib(String str, boolean z, String str2) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public boolean registerCallback(IOtaLibServiceCallBack iOtaLibServiceCallBack) throws RemoteException {
            return false;
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public boolean unregisterCallback() throws RemoteException {
            return false;
        }
    }

    void checkForUpdate(String str) throws RemoteException;

    void downloadConfigFiles(String str) throws RemoteException;

    void onStatusUpdateBackToLib(String str, boolean z, String str2) throws RemoteException;

    boolean registerCallback(IOtaLibServiceCallBack iOtaLibServiceCallBack) throws RemoteException;

    boolean unregisterCallback() throws RemoteException;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static abstract class Stub extends Binder implements IOtaLibService {
        static final int TRANSACTION_checkForUpdate = 2;
        static final int TRANSACTION_downloadConfigFiles = 5;
        static final int TRANSACTION_onStatusUpdateBackToLib = 4;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_unregisterCallback = 3;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IOtaLibService.DESCRIPTOR);
        }

        public static IOtaLibService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IOtaLibService.DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof IOtaLibService)) {
                return (IOtaLibService) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(IOtaLibService.DESCRIPTOR);
            }
            if (i == 1598968902) {
                parcel2.writeString(IOtaLibService.DESCRIPTOR);
                return true;
            }
            if (i == 1) {
                boolean registerCallback = registerCallback(IOtaLibServiceCallBack.Stub.asInterface(parcel.readStrongBinder()));
                parcel2.writeNoException();
                parcel2.writeInt(registerCallback ? 1 : 0);
            } else if (i == 2) {
                checkForUpdate(parcel.readString());
                parcel2.writeNoException();
            } else if (i == 3) {
                boolean unregisterCallback = unregisterCallback();
                parcel2.writeNoException();
                parcel2.writeInt(unregisterCallback ? 1 : 0);
            } else if (i == 4) {
                onStatusUpdateBackToLib(parcel.readString(), parcel.readInt() != 0, parcel.readString());
                parcel2.writeNoException();
            } else if (i == 5) {
                downloadConfigFiles(parcel.readString());
                parcel2.writeNoException();
            } else {
                return super.onTransact(i, parcel, parcel2, i2);
            }
            return true;
        }

        /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
        private static class Proxy implements IOtaLibService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IOtaLibService.DESCRIPTOR;
            }

            @Override // com.motorola.otalib.aidl.IOtaLibService
            public boolean registerCallback(IOtaLibServiceCallBack iOtaLibServiceCallBack) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IOtaLibService.DESCRIPTOR);
                    obtain.writeStrongInterface(iOtaLibServiceCallBack);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IOtaLibService
            public void checkForUpdate(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IOtaLibService.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IOtaLibService
            public boolean unregisterCallback() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IOtaLibService.DESCRIPTOR);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IOtaLibService
            public void onStatusUpdateBackToLib(String str, boolean z, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IOtaLibService.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeString(str2);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IOtaLibService
            public void downloadConfigFiles(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IOtaLibService.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
