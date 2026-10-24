package com.ncertbooks.engine.fake.hook;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.ncertbooks.engine.BlackBoxCore;
import com.ncertbooks.engine.fake.delegate.AppInstrumentation;

import com.ncertbooks.engine.fake.service.HCallbackProxy;
import com.ncertbooks.engine.fake.service.IAccessibilityManagerProxy;
import com.ncertbooks.engine.fake.service.IAccountManagerProxy;
import com.ncertbooks.engine.fake.service.IActivityClientProxy;
import com.ncertbooks.engine.fake.service.IActivityManagerProxy;
import com.ncertbooks.engine.fake.service.IActivityTaskManagerProxy;
import com.ncertbooks.engine.fake.service.IAlarmManagerProxy;
import com.ncertbooks.engine.fake.service.IAppOpsManagerProxy;
import com.ncertbooks.engine.fake.service.IAppWidgetManagerProxy;
import com.ncertbooks.engine.fake.service.IAttributionSourceProxy;
import com.ncertbooks.engine.fake.service.IAutofillManagerProxy;
import com.ncertbooks.engine.fake.service.ISensitiveContentProtectionManagerProxy;
import com.ncertbooks.engine.fake.service.ISettingsSystemProxy;
import com.ncertbooks.engine.fake.service.IConnectivityManagerProxy;
import com.ncertbooks.engine.fake.service.ISystemSensorManagerProxy;
import com.ncertbooks.engine.fake.service.IContentProviderProxy;
import com.ncertbooks.engine.fake.service.IXiaomiAttributionSourceProxy;
import com.ncertbooks.engine.fake.service.IXiaomiSettingsProxy;
import com.ncertbooks.engine.fake.service.IXiaomiMiuiServicesProxy;
import com.ncertbooks.engine.fake.service.IDnsResolverProxy;
import com.ncertbooks.engine.fake.service.IContextHubServiceProxy;
import com.ncertbooks.engine.fake.service.IDeviceIdentifiersPolicyProxy;
import com.ncertbooks.engine.fake.service.IDevicePolicyManagerProxy;
import com.ncertbooks.engine.fake.service.IDisplayManagerProxy;
import com.ncertbooks.engine.fake.service.IFingerprintManagerProxy;
import com.ncertbooks.engine.fake.service.IGraphicsStatsProxy;
import com.ncertbooks.engine.fake.service.IJobServiceProxy;
import com.ncertbooks.engine.fake.service.ILauncherAppsProxy;
import com.ncertbooks.engine.fake.service.ILocationManagerProxy;
import com.ncertbooks.engine.fake.service.IMediaRouterServiceProxy;
import com.ncertbooks.engine.fake.service.IMediaSessionManagerProxy;
import com.ncertbooks.engine.fake.service.IAudioServiceProxy;
import com.ncertbooks.engine.fake.service.ISensorPrivacyManagerProxy;
import com.ncertbooks.engine.fake.service.ContentResolverProxy;
import com.ncertbooks.engine.fake.service.IMiuiSecurityManagerProxy;
import com.ncertbooks.engine.fake.service.SystemLibraryProxy;
import com.ncertbooks.engine.fake.service.ReLinkerProxy;
import com.ncertbooks.engine.fake.service.MediaRecorderProxy;
import com.ncertbooks.engine.fake.service.NetworkPermissionCompat;
import com.ncertbooks.engine.fake.service.AudioRecordProxy;
import com.ncertbooks.engine.fake.service.MediaRecorderClassProxy;
import com.ncertbooks.engine.fake.service.SQLiteDatabaseProxy;
import com.ncertbooks.engine.fake.service.ClassLoaderProxy;
import com.ncertbooks.engine.fake.service.FileSystemProxy;
import com.ncertbooks.engine.fake.service.GmsProxy;
import com.ncertbooks.engine.fake.service.LevelDbProxy;
import com.ncertbooks.engine.fake.service.DeviceIdProxy;
import com.ncertbooks.engine.fake.service.GoogleAccountManagerProxy;
import com.ncertbooks.engine.fake.service.AuthenticationProxy;
import com.ncertbooks.engine.fake.service.AndroidIdProxy;
import com.ncertbooks.engine.fake.service.AudioPermissionProxy;
import com.ncertbooks.engine.fake.service.NetworkPermissionCompat;

import com.ncertbooks.engine.fake.service.INetworkManagementServiceProxy;
import com.ncertbooks.engine.fake.service.INotificationManagerProxy;
import com.ncertbooks.engine.fake.service.IPackageManagerProxy;
import com.ncertbooks.engine.fake.service.IPermissionManagerProxy;
import com.ncertbooks.engine.fake.service.IPersistentDataBlockServiceProxy;
import com.ncertbooks.engine.fake.service.IPhoneSubInfoProxy;
import com.ncertbooks.engine.fake.service.IPowerManagerProxy;
import com.ncertbooks.engine.fake.service.ApkAssetsProxy;
import com.ncertbooks.engine.fake.service.ResourcesManagerProxy;
import com.ncertbooks.engine.fake.service.IShortcutManagerProxy;
import com.ncertbooks.engine.fake.service.IStorageManagerProxy;
import com.ncertbooks.engine.fake.service.IStorageStatsManagerProxy;
import com.ncertbooks.engine.fake.service.ISystemUpdateProxy;
import com.ncertbooks.engine.fake.service.ITelephonyManagerProxy;
import com.ncertbooks.engine.fake.service.ITelephonyRegistryProxy;
import com.ncertbooks.engine.fake.service.IUserManagerProxy;
import com.ncertbooks.engine.fake.service.IVibratorServiceProxy;
import com.ncertbooks.engine.fake.service.IVpnManagerProxy;
import com.ncertbooks.engine.fake.service.IWifiManagerProxy;
import com.ncertbooks.engine.fake.service.IWifiScannerProxy;
import com.ncertbooks.engine.fake.service.IWindowManagerProxy;
import com.ncertbooks.engine.fake.service.context.ContentServiceStub;
import com.ncertbooks.engine.fake.service.context.RestrictionsManagerStub;
import com.ncertbooks.engine.fake.service.libcore.OsStub;
import com.ncertbooks.engine.utils.Slog;
import com.ncertbooks.engine.utils.compat.BuildCompat;
import com.ncertbooks.engine.fake.service.ISettingsProviderProxy;
import com.ncertbooks.engine.fake.service.FeatureFlagUtilsProxy;
import com.ncertbooks.engine.fake.service.WorkManagerProxy;



public class HookManager {
    public static final String TAG = "HookManager";

    private static final HookManager sHookManager = new HookManager();

    private final Map<Class<?>, IInjectHook> mInjectors = new HashMap<>();

    public static HookManager get() {
        return sHookManager;
    }

    public void init() {
        if (BlackBoxCore.get().isBlackProcess() || BlackBoxCore.get().isServerProcess()) {
            addInjector(new IDisplayManagerProxy());
            addInjector(new OsStub());
            addInjector(new IActivityManagerProxy());
            addInjector(new IPackageManagerProxy());
            addInjector(new ITelephonyManagerProxy());
            addInjector(new HCallbackProxy());
            addInjector(new IAppOpsManagerProxy());
            addInjector(new INotificationManagerProxy());
            addInjector(new IAlarmManagerProxy());
            addInjector(new IAppWidgetManagerProxy());
            addInjector(new ContentServiceStub());
            addInjector(new IWindowManagerProxy());
            addInjector(new IUserManagerProxy());
            addInjector(new RestrictionsManagerStub());
            addInjector(new IMediaSessionManagerProxy());
            addInjector(new IAudioServiceProxy());
            addInjector(new ISensorPrivacyManagerProxy());
            addInjector(new ContentResolverProxy());
            addInjector(new SystemLibraryProxy());
            addInjector(new ReLinkerProxy());
            addInjector(new WorkManagerProxy());
            addInjector(new MediaRecorderProxy());
            addInjector(new AudioRecordProxy());
            addInjector(new IMiuiSecurityManagerProxy());
            addInjector(new ISettingsProviderProxy());
            addInjector(new FeatureFlagUtilsProxy());
            addInjector(new MediaRecorderClassProxy());
            addInjector(new SQLiteDatabaseProxy());
            addInjector(new ClassLoaderProxy());
            addInjector(new FileSystemProxy());
            addInjector(new GmsProxy());
            addInjector(new LevelDbProxy());
            addInjector(new DeviceIdProxy());
            addInjector(new GoogleAccountManagerProxy());
            addInjector(new AuthenticationProxy());
            addInjector(new AndroidIdProxy());
            addInjector(new AudioPermissionProxy());
            addInjector(new ILocationManagerProxy());
            addInjector(new IStorageManagerProxy());
            addInjector(new ILauncherAppsProxy());
            addInjector(new IJobServiceProxy());
            addInjector(new IAccessibilityManagerProxy());
            addInjector(new ITelephonyRegistryProxy());
            addInjector(new IDevicePolicyManagerProxy());
            addInjector(new IAccountManagerProxy());
            addInjector(new NetworkPermissionCompat());
            addInjector(new IConnectivityManagerProxy());
            addInjector(new IDnsResolverProxy());
                    addInjector(new IAttributionSourceProxy());
        addInjector(new IContentProviderProxy());
        addInjector(new ISettingsSystemProxy());
        addInjector(new ISystemSensorManagerProxy());
        
        
        addInjector(new IXiaomiAttributionSourceProxy());
        addInjector(new IXiaomiSettingsProxy());
        addInjector(new IXiaomiMiuiServicesProxy());
            addInjector(new IPhoneSubInfoProxy());
            addInjector(new IMediaRouterServiceProxy());
            addInjector(new IPowerManagerProxy());
            addInjector(new IContextHubServiceProxy());
            
            addInjector(new IVibratorServiceProxy());
            addInjector(new IPersistentDataBlockServiceProxy());
            addInjector(AppInstrumentation.get());
            
            addInjector(new IWifiManagerProxy());
            addInjector(new IWifiScannerProxy());
            addInjector(new ApkAssetsProxy());
            addInjector(new ResourcesManagerProxy());
            
            if (BuildCompat.isS()) {
                addInjector(new IActivityClientProxy(null));
                addInjector(new IVpnManagerProxy());
            }
            
            if (BuildCompat.isS()) {
                addInjector(new ISensitiveContentProtectionManagerProxy());
            }
            
            if (BuildCompat.isR()) {
                addInjector(new IPermissionManagerProxy());
            }
            
            if (BuildCompat.isQ()) {
                addInjector(new IActivityTaskManagerProxy());
            }
            
            if (BuildCompat.isPie()) {
                addInjector(new ISystemUpdateProxy());
            }
            
            if (BuildCompat.isOreo()) {
                addInjector(new IAutofillManagerProxy());
                addInjector(new IDeviceIdentifiersPolicyProxy());
                addInjector(new IStorageStatsManagerProxy());
            }
            
            if (BuildCompat.isN_MR1()) {
                addInjector(new IShortcutManagerProxy());
            }
            
            if (BuildCompat.isN()) {
                addInjector(new INetworkManagementServiceProxy());
            }
            
            if (BuildCompat.isM()) {
                addInjector(new IFingerprintManagerProxy());
                addInjector(new IGraphicsStatsProxy());
            }
            
            if (BuildCompat.isL()) {
                addInjector(new IJobServiceProxy());
            }
        }
        injectAll();
    }

    public void checkEnv(Class<?> clazz) {
        IInjectHook iInjectHook = mInjectors.get(clazz);
        if (iInjectHook != null && iInjectHook.isBadEnv()) {
            Log.d(TAG, "checkEnv: " + clazz.getSimpleName() + " is bad env");
            iInjectHook.injectHook();
        }
    }

    public void checkAll() {
        for (Class<?> aClass : mInjectors.keySet()) {
            IInjectHook iInjectHook = mInjectors.get(aClass);
            if (iInjectHook != null && iInjectHook.isBadEnv()) {
                Log.d(TAG, "checkEnv: " + aClass.getSimpleName() + " is bad env");
                iInjectHook.injectHook();
            }
        }
    }

    void addInjector(IInjectHook injectHook) {
        mInjectors.put(injectHook.getClass(), injectHook);
    }

    void injectAll() {
        for (IInjectHook value : mInjectors.values()) {
            try {
                Slog.d(TAG, "hook: " + value);
                value.injectHook();
            } catch (Exception e) {
                Slog.d(TAG, "hook error: " + value);
                
                handleHookError(value, e);
            }
        }
    }

    
    private void handleHookError(IInjectHook hook, Exception e) {
        String hookName = hook.getClass().getSimpleName();
        
        
        Slog.e(TAG, "Hook failed: " + hookName + " - " + e.getMessage(), e);
        
        
        if (hookName.contains("ActivityManager") || 
            hookName.contains("PackageManager") ||
            hookName.contains("WebView") ||
            hookName.contains("ContentProvider")) {
            
            Slog.w(TAG, "Critical hook failed: " + hookName + ", attempting recovery");
            
            try {
                
                if (hook.isBadEnv()) {
                    Slog.d(TAG, "Attempting to recover hook: " + hookName);
                    hook.injectHook();
                }
            } catch (Exception recoveryException) {
                Slog.e(TAG, "Hook recovery failed: " + hookName, recoveryException);
            }
        }
    }

    
    public boolean areCriticalHooksInstalled() {
        String[] criticalHooks = {
            "IActivityManagerProxy",
            "IPackageManagerProxy",
            "IContentProviderProxy"
        };
        
        for (String hookName : criticalHooks) {
            boolean found = false;
            for (Class<?> hookClass : mInjectors.keySet()) {
                if (hookClass.getSimpleName().equals(hookName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Slog.w(TAG, "Critical hook missing: " + hookName);
                return false;
            }
        }
        
        Slog.d(TAG, "All critical hooks are installed");
        return true;
    }

    
    public void reinitializeHooks() {
        Slog.d(TAG, "Reinitializing all hooks");
        
        
        mInjectors.clear();
        
        
        init();
        
        Slog.d(TAG, "Hook reinitialization completed");
    }
}
