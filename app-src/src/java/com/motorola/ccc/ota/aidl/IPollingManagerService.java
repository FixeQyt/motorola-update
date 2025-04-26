package com.motorola.ccc.ota.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public interface IPollingManagerService extends IInterface {
    public static final String DESCRIPTOR = "com.motorola.ccc.ota.aidl.IPollingManagerService";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class Default implements IPollingManagerService {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // com.motorola.ccc.ota.aidl.IPollingManagerService
        public int registerApp(int i, String str, String str2, String[] strArr, long j, boolean z, boolean z2) throws RemoteException {
            return 0;
        }

        @Override // com.motorola.ccc.ota.aidl.IPollingManagerService
        public int unregisterApp(int i, String str) throws RemoteException {
            return 0;
        }
    }

    int registerApp(int i, String str, String str2, String[] strArr, long j, boolean z, boolean z2) throws RemoteException;

    int unregisterApp(int i, String str) throws RemoteException;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static abstract class Stub extends Binder implements IPollingManagerService {
        static final int TRANSACTION_registerApp = 1;
        static final int TRANSACTION_unregisterApp = 2;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IPollingManagerService.DESCRIPTOR);
        }

        public static IPollingManagerService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IPollingManagerService.DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof IPollingManagerService)) {
                return (IPollingManagerService) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(IPollingManagerService.DESCRIPTOR);
            }
            if (i == 1598968902) {
                parcel2.writeString(IPollingManagerService.DESCRIPTOR);
                return true;
            }
            if (i == 1) {
                int registerApp = registerApp(parcel.readInt(), parcel.readString(), parcel.readString(), parcel.createStringArray(), parcel.readLong(), parcel.readInt() != 0, parcel.readInt() != 0);
                parcel2.writeNoException();
                parcel2.writeInt(registerApp);
            } else if (i == 2) {
                int unregisterApp = unregisterApp(parcel.readInt(), parcel.readString());
                parcel2.writeNoException();
                parcel2.writeInt(unregisterApp);
            } else {
                return super.onTransact(i, parcel, parcel2, i2);
            }
            return true;
        }

        /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
        private static class Proxy implements IPollingManagerService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IPollingManagerService.DESCRIPTOR;
            }

            @Override // com.motorola.ccc.ota.aidl.IPollingManagerService
            public int registerApp(int i, String str, String str2, String[] strArr, long j, boolean z, boolean z2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IPollingManagerService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeStringArray(strArr);
                    obtain.writeLong(j);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeInt(z2 ? 1 : 0);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.motorola.ccc.ota.aidl.IPollingManagerService
            public int unregisterApp(int i, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IPollingManagerService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
