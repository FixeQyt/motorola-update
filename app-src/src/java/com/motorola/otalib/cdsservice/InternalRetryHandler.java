package com.motorola.otalib.cdsservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;
import com.motorola.otalib.cdsservice.webdataobjects.builders.WebResponseBuilder;
import org.json.JSONException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class InternalRetryHandler extends Binder implements Parcelable {
    public static final Parcelable.Creator<InternalRetryHandler> CREATOR = new Parcelable.Creator<InternalRetryHandler>() { // from class: com.motorola.otalib.cdsservice.InternalRetryHandler.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public InternalRetryHandler createFromParcel(Parcel parcel) {
            return new InternalRetryHandler(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public InternalRetryHandler[] newArray(int i) {
            return new InternalRetryHandler[i];
        }
    };
    private static final String DESCRIPTOR = "com.motorola.cds.webservice.WebServiceRetryHandler";
    private static final int TRANSACTION_retryHandler = 1;
    private RetryHandler handler;
    private IBinder remote;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStrongBinder(this);
    }

    public InternalRetryHandler(RetryHandler retryHandler) {
        this.handler = retryHandler;
    }

    private InternalRetryHandler(Parcel parcel) {
        this.remote = parcel.readStrongBinder();
    }

    @Override // android.os.Binder
    protected boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        int i3;
        if (i != 1) {
            if (i == 1598968902) {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
            return super.onTransact(i, parcel, parcel2, i2);
        }
        parcel.enforceInterface(DESCRIPTOR);
        String readString = parcel.readString();
        CDSLogger.d(CDSLogger.TAG, "InternalRetryHandler:onTransact() response string from WebService" + readString);
        RetryHandler retryHandler = this.handler;
        if (retryHandler != null) {
            i3 = retryHandler.retryRequest(WebResponseBuilder.from(readString));
        } else {
            CDSLogger.d(CDSLogger.TAG, "InternalRetryHandler:onTransact() retry handler is null");
            i3 = 0;
        }
        parcel2.writeNoException();
        parcel2.writeInt(i3);
        return true;
    }

    public boolean invokeRetryHandler(WebResponse webResponse) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        boolean z = false;
        try {
            try {
                try {
                    obtain.writeInterfaceToken(DESCRIPTOR);
                    obtain.writeString(WebResponseBuilder.toJSONString(webResponse));
                    this.remote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 1) {
                        z = true;
                    }
                } catch (JSONException e) {
                    CDSLogger.e(CDSLogger.TAG, "Caught json exception while invoking retryHandler " + e);
                }
            } catch (RemoteException e2) {
                CDSLogger.e(CDSLogger.TAG, "Caught remote exception while invoking retryHandler " + e2);
            }
            return z;
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }
}
