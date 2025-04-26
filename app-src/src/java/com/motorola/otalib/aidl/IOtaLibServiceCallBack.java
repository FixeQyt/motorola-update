package com.motorola.otalib.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface IOtaLibServiceCallBack extends IInterface {
    public static final String DESCRIPTOR = "com.motorola.otalib.aidl.IOtaLibServiceCallBack";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class Default implements IOtaLibServiceCallBack {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // com.motorola.otalib.aidl.IOtaLibServiceCallBack
        public void onConfigUpdateStatus(boolean z, String str) throws RemoteException {
        }

        @Override // com.motorola.otalib.aidl.IOtaLibServiceCallBack
        public void onStatusUpdate(String str, boolean z, String str2) throws RemoteException {
        }
    }

    void onConfigUpdateStatus(boolean z, String str) throws RemoteException;

    void onStatusUpdate(String str, boolean z, String str2) throws RemoteException;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static abstract class Stub extends Binder implements IOtaLibServiceCallBack {
        static final int TRANSACTION_onConfigUpdateStatus = 2;
        static final int TRANSACTION_onStatusUpdate = 1;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IOtaLibServiceCallBack.DESCRIPTOR);
        }

        public static IOtaLibServiceCallBack asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IOtaLibServiceCallBack.DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof IOtaLibServiceCallBack)) {
                return (IOtaLibServiceCallBack) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(IOtaLibServiceCallBack.DESCRIPTOR);
            }
            if (i == 1598968902) {
                parcel2.writeString(IOtaLibServiceCallBack.DESCRIPTOR);
                return true;
            }
            if (i == 1) {
                onStatusUpdate(parcel.readString(), parcel.readInt() != 0, parcel.readString());
                parcel2.writeNoException();
            } else if (i == 2) {
                onConfigUpdateStatus(parcel.readInt() != 0, parcel.readString());
                parcel2.writeNoException();
            } else {
                return super.onTransact(i, parcel, parcel2, i2);
            }
            return true;
        }

        /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
        private static class Proxy implements IOtaLibServiceCallBack {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IOtaLibServiceCallBack.DESCRIPTOR;
            }

            @Override // com.motorola.otalib.aidl.IOtaLibServiceCallBack
            public void onStatusUpdate(String str, boolean z, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IOtaLibServiceCallBack.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeString(str2);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.otalib.aidl.IOtaLibServiceCallBack
            public void onConfigUpdateStatus(boolean z, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IOtaLibServiceCallBack.DESCRIPTOR);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeString(str);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
