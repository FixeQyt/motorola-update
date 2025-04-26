package com.motorola.ccc.ota.sources.fota;

import java.util.HashMap;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class FotaUpdateTypes {
    private static FotaUpdateTypes me;
    private final Map<Type, FotaUpdateType> sources;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public enum Type {
        USER_INITIATED,
        DEVICE_INITIATED,
        NETWORK_INITIATED
    }

    private FotaUpdateTypes() {
        HashMap hashMap = new HashMap();
        this.sources = hashMap;
        hashMap.put(Type.DEVICE_INITIATED, new ClientInitiatedFotaUpdate());
        hashMap.put(Type.USER_INITIATED, new UserInitiatedFotaUpdate());
        hashMap.put(Type.NETWORK_INITIATED, new NetworkInitiatedFotaUpdate());
    }

    public static FotaUpdateTypes getInstance() {
        if (me == null) {
            me = new FotaUpdateTypes();
        }
        return me;
    }

    public FotaUpdateType getUpgradeSource(Type type) {
        return this.sources.get(type);
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class ClientInitiatedFotaUpdate extends FotaUpdateType {
        public ClientInitiatedFotaUpdate() {
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class NetworkInitiatedFotaUpdate extends FotaUpdateType {
        public NetworkInitiatedFotaUpdate() {
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class UserInitiatedFotaUpdate extends FotaUpdateType {
        @Override // com.motorola.ccc.ota.sources.fota.FotaUpdateType
        public boolean isDownloadVisible() {
            return true;
        }

        public UserInitiatedFotaUpdate() {
        }
    }
}
