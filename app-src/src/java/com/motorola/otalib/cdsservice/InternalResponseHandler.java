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
public class InternalResponseHandler extends Binder implements Parcelable {
    public static final Parcelable.Creator<InternalResponseHandler> CREATOR = new Parcelable.Creator<InternalResponseHandler>() { // from class: com.motorola.otalib.cdsservice.InternalResponseHandler.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public InternalResponseHandler createFromParcel(Parcel parcel) {
            return new InternalResponseHandler(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public InternalResponseHandler[] newArray(int i) {
            return new InternalResponseHandler[i];
        }
    };
    private static final String DESCRIPTOR = "com.motorola.cds.webservice.WebServiceResponseHandler";
    private static final int TRANSACTION_handleResponse = 1;
    private ResponseHandler handler;
    private IBinder remote;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStrongBinder(this);
    }

    public InternalResponseHandler(ResponseHandler responseHandler) {
        this.handler = responseHandler;
    }

    private InternalResponseHandler(Parcel parcel) {
        this.remote = parcel.readStrongBinder();
    }

    @Override // android.os.Binder
    protected boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        if (i != 1) {
            if (i == 1598968902) {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
            return super.onTransact(i, parcel, parcel2, i2);
        }
        parcel.enforceInterface(DESCRIPTOR);
        String readString = parcel.readString();
        CDSLogger.d(CDSLogger.TAG, "InternalResponseHandler:onTransact() response string from WebService" + readString);
        ResponseHandler responseHandler = this.handler;
        if (responseHandler != null) {
            responseHandler.handleResponse(WebResponseBuilder.from(readString));
        } else {
            CDSLogger.d(CDSLogger.TAG, "InternalResponseHandler:onTransact() handler is null");
        }
        parcel2.writeNoException();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void invokeHandleResponse(WebResponse webResponse) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeString(WebResponseBuilder.toJSONString(webResponse));
                this.remote.transact(1, obtain, obtain2, 0);
                obtain2.readException();
            } catch (RemoteException e) {
                CDSLogger.e(CDSLogger.TAG, "Caught remote exception while invoking handleResponse " + e);
            } catch (JSONException e2) {
                CDSLogger.e(CDSLogger.TAG, "Caught json exception while invoking handleResponse " + e2);
            }
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }
}
