package com.motorola.ccc.ota.installer;

import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.installer.updaterEngine.UpdaterEngineInstaller;
import com.motorola.ccc.ota.installer.updaterEngine.common.InstallerUtilMethods;
import com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptor;
import com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptorExceptionHandler;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import org.json.JSONException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class InstallTypeResolver {
    private static Installer installerType;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public enum InstallerType {
        AB,
        STREAMING
    }

    public static Installer getInstallTypeHandler(ApplicationEnv applicationEnv, BotaSettings botaSettings, ApplicationEnv.Database database, ApplicationEnv.Database.Descriptor descriptor, CusSM cusSM) {
        if (descriptor == null || descriptor.getMeta() == null) {
            return null;
        }
        UpdaterEngineInstaller builder = UpdaterEngineInstaller.builder(descriptor.getMeta().getAbInstallType(), applicationEnv, botaSettings, database, descriptor, cusSM);
        installerType = builder;
        return builder;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public interface Installer {
        default void allocateSpaceBeforeApplyPatch(ApplicationEnv.Database.Descriptor descriptor) throws JSONException {
        }

        void checkAndResetInstallerFromCusSM(ApplicationEnv.PackageState packageState, InstallerUtilMethods.InstallerErrorStatus installerErrorStatus);

        void displayScreenForGettingDescriptor(ApplicationEnv.Database.Descriptor descriptor);

        boolean doesDownloadedFileClearedFromDisk();

        InstallerType getCurrentInstallerType();

        InstallerUtilMethods.InstallerErrorStatus initializeUpdaterEngineHandlerMergeState();

        default void onAllocateSpaceResult() {
        }

        void onInternalNotification(String str, ApplicationEnv.Database.Descriptor descriptor);

        void onStartDownloadNotification(String str);

        void promptUpgradeActivity(ApplicationEnv.Database.Descriptor descriptor) throws JSONException;

        void promptUpgradeNotification(ApplicationEnv.Database.Descriptor descriptor) throws JSONException;

        boolean shouldPromptUpgradeNotification(ApplicationEnv.Database.Descriptor descriptor) throws JSONException;

        InstallerUtilMethods.InstallerErrorStatus updaterEngineHandler(ApplicationEnv.Database.Descriptor descriptor, SystemUpdaterPolicy systemUpdaterPolicy);

        default boolean isDataSpaceLowForUpgrade(ApplicationEnv.Database.Descriptor descriptor, ApplicationEnv applicationEnv) {
            return FileUtils.isDataMemoryLow(applicationEnv, descriptor.getMeta().getSize(), descriptor.getMeta().getExtraSpace());
        }

        default void clearRetryTasks() {
            if (ResetSMToGettingDescriptor.isRetryPending()) {
                ResetSMToGettingDescriptor.clearRetryTask();
            }
            if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                ResetSMToGettingDescriptorExceptionHandler.clearRetryTask();
            }
        }
    }
}
