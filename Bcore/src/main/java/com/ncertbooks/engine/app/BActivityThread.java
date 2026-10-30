package com.ncertbooks.engine.app;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.app.job.JobService;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.InMemoryDexClassLoader;

import ref.android.app.ActivityThreadAppBindDataContext;
import ref.android.app.BRActivity;
import ref.android.app.BRActivityManagerNative;
import ref.android.app.BRActivityThread;
import ref.android.app.BRActivityThreadActivityClientRecord;
import ref.android.app.BRActivityThreadAppBindData;
import ref.android.app.BRActivityThreadNMR1;
import ref.android.app.BRActivityThreadQ;
import ref.android.app.BRContextImpl;
import ref.android.app.BRLoadedApk;
import ref.android.app.BRService;
import ref.android.app.LoadedApk;
import ref.android.content.BRBroadcastReceiver;
import ref.android.content.BRContentProviderClient;
import ref.android.graphics.BRCompatibility;
import ref.android.security.net.config.BRNetworkSecurityConfigProvider;
import ref.com.android.internal.content.BRReferrerIntent;
import ref.dalvik.system.BRVMRuntime;
import com.ncertbooks.engine.BlackBoxCore;
import com.ncertbooks.engine.app.configuration.AppLifecycleCallback;
import com.ncertbooks.engine.app.dispatcher.AppServiceDispatcher;
import com.ncertbooks.engine.core.CrashHandler;
import com.ncertbooks.engine.core.IBActivityThread;
import com.ncertbooks.engine.core.IOCore;
import com.ncertbooks.engine.core.NativeCore;
import com.ncertbooks.engine.core.env.VirtualRuntime;
import com.ncertbooks.engine.core.system.user.BUserHandle;
import com.ncertbooks.engine.entity.AppConfig;
import com.ncertbooks.engine.entity.am.ReceiverData;

import com.ncertbooks.engine.fake.delegate.AppInstrumentation;
import com.ncertbooks.engine.fake.delegate.ContentProviderDelegate;

import com.ncertbooks.engine.fake.hook.HookManager;
import com.ncertbooks.engine.fake.service.HCallbackProxy;
import com.ncertbooks.engine.utils.Reflector;
import com.ncertbooks.engine.utils.SafeContextWrapper;
import com.ncertbooks.engine.utils.GlobalContextWrapper;
import com.ncertbooks.engine.utils.Slog;
import com.ncertbooks.engine.utils.compat.ActivityManagerCompat;
import com.ncertbooks.engine.utils.compat.BuildCompat;
import com.ncertbooks.engine.utils.compat.ContextCompat;
import com.ncertbooks.engine.utils.compat.StrictModeCompat;


public class BActivityThread extends IBActivityThread.Stub {
    public static final String TAG = "BActivityThread";

    private static BActivityThread sBActivityThread;
    private AppBindData mBoundApplication;
    private Application mInitialApplication;
    private AppConfig mAppConfig;
    private final List<ProviderInfo> mProviders = new ArrayList<>();
    private final Handler mH = BlackBoxCore.get().getHandler();
    private static final Object mConfigLock = new Object();

    public static boolean isThreadInit() {
        return sBActivityThread != null;
    }

    public static BActivityThread currentActivityThread() {
        if (sBActivityThread == null) {
            synchronized (BActivityThread.class) {
                if (sBActivityThread == null) {
                    sBActivityThread = new BActivityThread();
                }
            }
        }
        return sBActivityThread;
    }

    public static AppConfig getAppConfig() {
        synchronized (mConfigLock) {
            return currentActivityThread().mAppConfig;
        }
    }

    public static List<ProviderInfo> getProviders() {
        return currentActivityThread().mProviders;
    }

    public static String getAppProcessName() {
        if (getAppConfig() != null) {
            return getAppConfig().processName;
        } else if (currentActivityThread().mBoundApplication != null) {
            return currentActivityThread().mBoundApplication.processName;
        } else {
            return null;
        }
    }

    public static String getAppPackageName() {
        if (getAppConfig() != null) {
            return getAppConfig().packageName;
        } else if (currentActivityThread().mInitialApplication != null) {
            return currentActivityThread().mInitialApplication.getPackageName();
        } else {
            return null;
        }
    }

    public static Application getApplication() {
        return currentActivityThread().mInitialApplication;
    }

    public static int getAppPid() {
        return getAppConfig() == null ? -1 : getAppConfig().bpid;
    }

    public static int getBUid() {
        return getAppConfig() == null ? BUserHandle.AID_APP_START : getAppConfig().buid;
    }

    public static int getBAppId() {
        return BUserHandle.getAppId(getBUid());
    }

    public static int getCallingBUid() {
        return getAppConfig() == null ? BlackBoxCore.getHostUid() : getAppConfig().callingBUid;
    }

    public static int getUid() {
        return getAppConfig() == null ? -1 : getAppConfig().uid;
    }

    public static int getUserId() {
        return getAppConfig() == null ? 0 : getAppConfig().userId;
    }

    public void initProcess(AppConfig appConfig) {
        synchronized (mConfigLock) {
            if (this.mAppConfig != null && !this.mAppConfig.packageName.equals(appConfig.packageName)) {
                
                throw new RuntimeException("reject init process: " + appConfig.processName + ", this process is : " + this.mAppConfig.processName);
            }
            this.mAppConfig = appConfig;
            IBinder iBinder = asBinder();
            try {
                iBinder.linkToDeath(new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        synchronized (mConfigLock) {
                            try {
                                iBinder.linkToDeath(this, 0);
                            } catch (RemoteException ignored) {
                            }
                            mAppConfig = null;
                        }
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isInit() {
        return mBoundApplication != null;
    }

    public Service createService(ServiceInfo serviceInfo, IBinder token) {
        if (!BActivityThread.currentActivityThread().isInit()) {
            BActivityThread.currentActivityThread().bindApplication(serviceInfo.packageName, serviceInfo.processName);
        }
        ClassLoader classLoader = BRLoadedApk.get(mBoundApplication.info).getClassLoader();
        Service service;
        try {
            service = (Service) classLoader.loadClass(serviceInfo.name).newInstance();
        } catch (ClassNotFoundException e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services class not found, skipping: " + serviceInfo.name);
                return null;
            }
            e.printStackTrace();
            Slog.e(TAG, "Unable to instantiate service " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, "Unable to instantiate service " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        }

        try {
            Context context = BlackBoxCore.getContext().createPackageContext(
                    serviceInfo.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            BRContextImpl.get(context).setOuterContext(service);
            BRService.get(service).attach(
                    context,
                    BlackBoxCore.mainThread(),
                    serviceInfo.name,
                    token,
                    mInitialApplication,
                    BRActivityManagerNative.get().getDefault()
            );
            ContextCompat.fix(context);
            service.onCreate();
            return service;
        } catch (Exception e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services service creation failed, skipping: " + serviceInfo.name);
                return null;
            }
            Slog.w(TAG, "Service creation failed, but continuing: " + serviceInfo.name + " - " + e.getMessage());
            return null;
        }
    }

    public JobService createJobService(ServiceInfo serviceInfo) {
        if (!BActivityThread.currentActivityThread().isInit()) {
            BActivityThread.currentActivityThread().bindApplication(serviceInfo.packageName, serviceInfo.processName);
        }
        ClassLoader classLoader = BRLoadedApk.get(mBoundApplication.info).getClassLoader();
        JobService service;
        try {
            service = (JobService) classLoader.loadClass(serviceInfo.name).newInstance();
        } catch (ClassNotFoundException e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services JobService class not found, skipping: " + serviceInfo.name);
                return null;
            }
            e.printStackTrace();
            Slog.e(TAG, "Unable to create JobService " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, "Unable to create JobService " + serviceInfo.name
                    + ": " + e.toString());
            return null;
        }

        try {
            Context context = BlackBoxCore.getContext().createPackageContext(
                    serviceInfo.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            BRContextImpl.get(context).setOuterContext(service);
            BRService.get(service).attach(
                    context,
                    BlackBoxCore.mainThread(),
                    serviceInfo.name,
                    BActivityThread.currentActivityThread().getActivityThread(),
                    mInitialApplication,
                    BRActivityManagerNative.get().getDefault()
            );
            ContextCompat.fix(context);
            service.onCreate();
            service.onBind(null);
            return service;
        } catch (Exception e) {
            
            if (serviceInfo.name.contains("google.android.gms") || 
                serviceInfo.name.contains("google.android.location")) {
                Slog.w(TAG, "Google Play Services JobService creation failed, skipping: " + serviceInfo.name);
                return null;
            }
            Slog.w(TAG, "JobService creation failed, but continuing: " + serviceInfo.name + " - " + e.getMessage());
            return null;
        }
    }

    public void bindApplication(final String packageName, final String processName) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ConditionVariable conditionVariable = new ConditionVariable();
            BlackBoxCore.get().getHandler().post(() -> {
                handleBindApplication(packageName, processName);
                conditionVariable.open();
            });
            conditionVariable.block();
        } else {
            handleBindApplication(packageName, processName);
        }
    }

    public synchronized void handleBindApplication(String packageName, String processName) {
        if (isInit())
            return;
        try {
            CrashHandler.create();
        } catch (Throwable ignored) {
        }

        PackageInfo packageInfo = BlackBoxCore.getBPackageManager().getPackageInfo(packageName, PackageManager.GET_PROVIDERS, BActivityThread.getUserId());
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        // NOTE: Do NOT filter splitSourceDirs. Apps like Instagram (Helium framework) store DEX in
        // assets/<split>/classes.dex, not at root level. Filtering removes those splits from
        // splitSourceDirs, preventing the app's own ClassLoader from finding its module DEX.
        // DexPathList IOExceptions for non-standard splits are suppressed by Android and are harmless.
        if (packageInfo.providers == null) {
            packageInfo.providers = new ProviderInfo[]{};
        }
        mProviders.addAll(Arrays.asList(packageInfo.providers));

        Object boundApplication = BRActivityThread.get(BlackBoxCore.mainThread()).mBoundApplication();

        // Resolve split paths early — needed for BOTH createPackageContext (resources)
        // and preloadSplitDex (DEX). For FLAG_STORAGE installs the engine stores only
        // base.apk, so the engine's ApplicationInfo has null splitSourceDirs.
        String[] resolvedSplits = applicationInfo.splitSourceDirs;
        if (resolvedSplits == null || resolvedSplits.length == 0) {
            resolvedSplits = resolveHostSplitPaths(applicationInfo.packageName);
        }
        if (resolvedSplits != null && resolvedSplits.length > 0) {
            applicationInfo.splitSourceDirs = resolvedSplits;
            applicationInfo.splitPublicSourceDirs = resolvedSplits;
        }

        // Android 14+ (API 34) enforces strict split path validation in DexPathList.
        // Strip any split paths that no longer exist (e.g. app updated, system APK hash changed).
        if (Build.VERSION.SDK_INT >= 34 && applicationInfo.splitSourceDirs != null) {
            java.util.List<String> validSplits = new java.util.ArrayList<>();
            for (String p : applicationInfo.splitSourceDirs) {
                if (p != null && new java.io.File(p).exists()) validSplits.add(p);
            }
            if (validSplits.size() != applicationInfo.splitSourceDirs.length) {
                applicationInfo.splitSourceDirs = validSplits.isEmpty() ? null
                        : validSplits.toArray(new String[0]);
                applicationInfo.splitPublicSourceDirs = applicationInfo.splitSourceDirs;
            }
        }

        Context packageContext = createPackageContext(applicationInfo);
        Object loadedApk = BRContextImpl.get(packageContext).mPackageInfo();
        BRLoadedApk.get(loadedApk)._set_mSecurityViolation(false);

        // createPackageContext() does its own PM lookup and may return a LoadedApk whose
        // mSplitResDirs is null (engine's hooked PM returns FLAG_STORAGE ApplicationInfo
        // with no splits). Inject the resolved splits directly so ResourcesManager loads
        // the split resource APKs — without them, theme attribute resolution fails with
        // TypedValue{a=-1} / InflateException in the cloned app's first activity.
        if (applicationInfo.splitSourceDirs != null && applicationInfo.splitSourceDirs.length > 0) {
            try {
                Class<?> loadedApkClass = Class.forName("android.app.LoadedApk");
                Field splitResDirsField = loadedApkClass.getDeclaredField("mSplitResDirs");
                splitResDirsField.setAccessible(true);
                splitResDirsField.set(loadedApk, applicationInfo.splitSourceDirs);
                // Clear cached Resources so getResources() recreates with the new splits.
                Field mResourcesField = loadedApkClass.getDeclaredField("mResources");
                mResourcesField.setAccessible(true);
                mResourcesField.set(loadedApk, null);
                Slog.d(TAG, "injected mSplitResDirs (" + applicationInfo.splitSourceDirs.length + " splits)");
            } catch (Exception e) {
                Slog.w(TAG, "mSplitResDirs inject failed: " + e.getMessage());
            }
        }

        BRLoadedApk.get(loadedApk)._set_mApplicationInfo(applicationInfo);

        // Force createOrUpdateClassLoaderLocked to run with the correct splitSourceDirs.
        // If the LoadedApk came from the ActivityThread cache it may already have an
        // mClassLoader built without split paths, and mAppComponentFactory unset.
        // Clearing mClassLoader makes getClassLoader() recreate it — including splits
        // and mAppComponentFactory — before we wrap it in InMemoryDexClassLoader.
        try {
            Class<?> loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mClField = loadedApkClass.getDeclaredField("mClassLoader");
            mClField.setAccessible(true);
            mClField.set(loadedApk, null);
        } catch (Exception e) {
            Slog.w(TAG, "mClassLoader pre-clear failed: " + e.getMessage());
        }

        // Preload Helium/split-embedded DEX before makeApplication() so module
        // classloaders are ready when Application.onCreate() bootstraps Helium.
        preloadSplitDex(applicationInfo, loadedApk, BRLoadedApk.get(loadedApk).getClassLoader());

        int targetSdkVersion = applicationInfo.targetSdkVersion;
        if (targetSdkVersion < Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy newPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy()).permitNetwork().build();
            StrictMode.setThreadPolicy(newPolicy);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (targetSdkVersion < Build.VERSION_CODES.N) {
                StrictModeCompat.disableDeathOnFileUriExposure();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WebView.setDataDirectorySuffix(getUserId() + ":" + packageName + ":" + processName);
        }

        VirtualRuntime.setupRuntime(processName, applicationInfo);

        BRVMRuntime.get(BRVMRuntime.get().getRuntime()).setTargetSdkVersion(applicationInfo.targetSdkVersion);
        if (BuildCompat.isS()) {
            BRCompatibility.get().setTargetSdkVersion(applicationInfo.targetSdkVersion);
        }

        NativeCore.init(Build.VERSION.SDK_INT);
        assert packageContext != null;
        IOCore.get().enableRedirect(packageContext);

        AppBindData bindData = new AppBindData();
        bindData.appInfo = applicationInfo;
        bindData.processName = processName;
        bindData.info = loadedApk;
        bindData.providers = mProviders;

        ActivityThreadAppBindDataContext activityThreadAppBindData = BRActivityThreadAppBindData.get(boundApplication);
        activityThreadAppBindData._set_instrumentationName(new ComponentName(bindData.appInfo.packageName, Instrumentation.class.getName()));
        activityThreadAppBindData._set_appInfo(bindData.appInfo);
        activityThreadAppBindData._set_info(bindData.info);
        activityThreadAppBindData._set_processName(bindData.processName);
        activityThreadAppBindData._set_providers(bindData.providers);

        mBoundApplication = bindData;

        
        if (BRNetworkSecurityConfigProvider.getRealClass() != null) {
            Security.removeProvider("AndroidNSSP");
            BRNetworkSecurityConfigProvider.get().install(packageContext);
        }
        Application application;
        try {
            onBeforeCreateApplication(packageName, processName, packageContext);
            application = BRLoadedApk.get(loadedApk).makeApplication(false, null);
            if (application == null) {
                throw new RuntimeException("makeApplication returned null");
            }
            mInitialApplication = application;
            BRActivityThread.get(BlackBoxCore.mainThread())._set_mInitialApplication(mInitialApplication);
            ContextCompat.fix((Context) BRActivityThread.get(BlackBoxCore.mainThread()).getSystemContext());
            ContextCompat.fix(mInitialApplication);
            installProviders(mInitialApplication, bindData.processName, bindData.providers);

            onBeforeApplicationOnCreate(packageName, processName, application);
            AppInstrumentation.get().callApplicationOnCreate(application);
            onAfterApplicationOnCreate(packageName, processName, application);

            HookManager.get().checkEnv(HCallbackProxy.class);
        } catch (Throwable e) {
            Slog.e(TAG, "Critical error in handleBindApplication", e);
            throw new RuntimeException("Unable to makeApplication", e);
        }
    }



    /**
     * Check if a ContentProvider is a known anti-virtual-environment detector.
     * These providers detect sandboxed environments and force-kill the app.
     */
    private static boolean isAntiDetectProvider(String className) {
        String lower = className.toLowerCase();
        // Meituan Hades anti-cheat
        return lower.contains("hades")
                || lower.contains("ztuni")
                // Douyin/TikTok security
                || lower.contains("securityguard")
                || lower.contains("avdetector")
                // Common detection patterns
                || lower.contains("virtualdetect")
                || lower.contains("sandboxdetect")
                || lower.contains("emulatordetect")
                || lower.contains("fridadetect")
                || lower.contains("hookdetect")
                || lower.contains("xposeddetect")
                || lower.contains("magiskdetect");
    }
    
    
    /**
     * Walk every split APK and look for assets/<splitName>/classes.dex — the layout
     * used by Instagram's Helium framework. Collect all such DEX buffers into a single
     * InMemoryDexClassLoader chained on top of the LoadedApk's existing classloader,
     * then inject it back into the LoadedApk's mClassLoader and set it as the thread
     * context classloader.
     *
     * This runs BEFORE makeApplication() so Application.onCreate() sees the complete
     * classloader chain when Helium's bootstrapper calls loadClass(). InMemoryDexClassLoader
     * sidesteps Android 10+ writable-path restrictions and Android 16's useEmbeddedDex
     * enforcement since the bytes live only in heap memory.
     */
    /**
     * Returns split APK paths for {@code packageName}, trying three sources in order:
     * 1. Host PackageManager (original app still installed — fastest, most complete)
     * 2. Sandbox-copied splits in data/app/<pkg>/ (original uninstalled, CloneManagerModule copied them)
     * Returns null when no splits are found.
     */
    private static String[] resolveHostSplitPaths(String packageName) {
        // 1. Host PM
        try {
            android.content.pm.ApplicationInfo hostInfo = BlackBoxCore.getContext()
                    .getPackageManager()
                    .getApplicationInfo(packageName, 0);
            if (hostInfo.splitSourceDirs != null && hostInfo.splitSourceDirs.length > 0) {
                return hostInfo.splitSourceDirs;
            }
        } catch (Exception ignored) {}

        // 2. Sandbox copies
        java.io.File appDir = com.ncertbooks.engine.core.env.BEnvironment.getAppDir(packageName);
        if (appDir.exists()) {
            java.io.File[] splits = appDir.listFiles(
                    (dir, name) -> name.startsWith("split_") && name.endsWith(".apk"));
            if (splits != null && splits.length > 0) {
                String[] paths = new String[splits.length];
                for (int i = 0; i < splits.length; i++) paths[i] = splits[i].getAbsolutePath();
                return paths;
            }
        }
        return null;
    }

    private static void preloadSplitDex(ApplicationInfo applicationInfo, Object loadedApkInstance, ClassLoader baseLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        // By this point handleBindApplication has already called resolveHostSplitPaths and
        // populated applicationInfo.splitSourceDirs, so we can use it directly.
        // Fall back via resolveHostSplitPaths again in case something cleared it.
        String[] splitPaths = applicationInfo.splitSourceDirs;
        if (splitPaths == null || splitPaths.length == 0) {
            splitPaths = resolveHostSplitPaths(applicationInfo.packageName);
        }
        if (splitPaths == null || splitPaths.length == 0) return;

        List<ByteBuffer> dexBuffers = new ArrayList<>();
        for (String splitPath : splitPaths) {
            if (splitPath == null) continue;
            String fileName = new java.io.File(splitPath).getName();
            if (!fileName.startsWith("split_")) continue;
            int dot = fileName.lastIndexOf('.');
            String splitName = fileName.substring(6, dot > 6 ? dot : fileName.length());

            String assetEntry = "assets/" + splitName + "/classes.dex";
            try (ZipFile zip = new ZipFile(splitPath)) {
                ZipEntry entry = zip.getEntry(assetEntry);
                if (entry == null) continue;
                byte[] bytes = readStreamFully(zip.getInputStream(entry));
                dexBuffers.add(ByteBuffer.wrap(bytes));
                Slog.d(TAG, "preloadSplitDex: " + assetEntry + " (" + bytes.length + " B)");
            } catch (IOException e) {
                Slog.w(TAG, "preloadSplitDex: skip " + splitPath + ": " + e.getMessage());
            }
        }

        if (dexBuffers.isEmpty()) return;

        ClassLoader combined = new InMemoryDexClassLoader(
                dexBuffers.toArray(new ByteBuffer[0]), baseLoader);
        Slog.d(TAG, "preloadSplitDex: built InMemoryDexClassLoader (" + dexBuffers.size() + " DEXes)");

        // Inject into the real android.app.LoadedApk so getClassLoader() returns the combined chain.
        try {
            Class<?> cls = Class.forName("android.app.LoadedApk");
            Field f = cls.getDeclaredField("mClassLoader");
            f.setAccessible(true);
            f.set(loadedApkInstance, combined);
            Slog.d(TAG, "preloadSplitDex: mClassLoader injected");
        } catch (Exception e) {
            Slog.w(TAG, "preloadSplitDex: mClassLoader inject failed: " + e.getMessage());
        }

        // Thread context classloader: Helium reads this directly in some code paths.
        Thread.currentThread().setContextClassLoader(combined);
    }

    private static byte[] readStreamFully(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) baos.write(buf, 0, n);
            return baos.toByteArray();
        } finally {
            try { is.close(); } catch (IOException ignored) {}
        }
    }

    public static Context createPackageContext(ApplicationInfo info) {
        try {
            return BlackBoxCore.getContext().createPackageContext(info.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void installProviders(Context context, String processName, List<ProviderInfo> provider) {
        long origId = Binder.clearCallingIdentity();
        try {
            for (ProviderInfo providerInfo : provider) {
                try {
                    // Skip known anti-virtual-environment detection providers.
                    if (providerInfo.name != null && isAntiDetectProvider(providerInfo.name)) {
                        Slog.w(TAG, "Skipping anti-detect ContentProvider: " + providerInfo.name);
                        continue;
                    }
                    if (processName.equals(providerInfo.processName) ||
                            providerInfo.processName.equals(context.getPackageName()) || providerInfo.multiprocess) {
                        installProvider(BlackBoxCore.mainThread(), context, providerInfo, null);
                    }
                } catch (Throwable ignored) {
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
            ContentProviderDelegate.init();
        }
    }

    public Object getPackageInfo() {
        return mBoundApplication.info;
    }

    public static void installProvider(Object mainThread, Context context, ProviderInfo providerInfo, Object holder) throws Throwable {
        Method installProvider = Reflector.findMethodByFirstName(mainThread.getClass(), "installProvider");
        if (installProvider != null) {
            installProvider.setAccessible(true);
            installProvider.invoke(mainThread, context, holder, providerInfo, false, true, true);
        }
    }



    @Override
    public IBinder getActivityThread() {
        return BRActivityThread.get(BlackBoxCore.mainThread()).getApplicationThread();
    }

    @Override
    public void bindApplication() {
        if (!isInit()) {
            bindApplication(getAppPackageName(), getAppProcessName());
        }
    }

    @Override
    public void stopService(Intent intent) {
        AppServiceDispatcher.get().stopService(intent);
    }

    @Override
    public void restartJobService(String selfId) throws RemoteException {

    }

    @Override
    public IBinder acquireContentProviderClient(ProviderInfo providerInfo) throws RemoteException {
        if (!isInit()) {
            bindApplication(BActivityThread.getAppConfig().packageName, BActivityThread.getAppConfig().processName);
        }
        String[] split = providerInfo.authority.split(";");
        for (String auth : split) {
            ContentProviderClient contentProviderClient = BlackBoxCore.getContext()
                    .getContentResolver().acquireContentProviderClient(auth);
            IInterface iInterface = BRContentProviderClient.get(contentProviderClient).mContentProvider();
            if (iInterface == null)
                continue;
            return iInterface.asBinder();
        }
        return null;
    }

    @Override
    public IBinder peekService(Intent intent) {
        return AppServiceDispatcher.get().peekService(intent);
    }

    @Override
    public void finishActivity(final IBinder token) {
        mH.post(() -> {
            Map<IBinder, Object> activities = BRActivityThread.get(BlackBoxCore.mainThread()).mActivities();
            if (activities.isEmpty())
                return;
            Object clientRecord = activities.get(token);
            if (clientRecord == null)
                return;
            Activity activity = getActivityByToken(token);

            while (activity.getParent() != null) {
                activity = activity.getParent();
            }

            int resultCode = BRActivity.get(activity).mResultCode();
            Intent resultData = BRActivity.get(activity).mResultData();
            ActivityManagerCompat.finishActivity(token, resultCode, resultData);
            BRActivity.get(activity)._set_mFinished(true);
        });
    }

    @Override
    public void handleNewIntent(final IBinder token, final Intent intent) {
        mH.post(() -> {
            Intent newIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                newIntent = BRReferrerIntent.get()._new(intent, BlackBoxCore.getHostPkg());
            } else {
                newIntent = intent;
            }
            Object mainThread = BlackBoxCore.mainThread();
            if (BRActivityThread.get(BlackBoxCore.mainThread())._check_performNewIntents(null, null) != null) {
                BRActivityThread.get(mainThread).performNewIntents(
                        token,
                        Collections.singletonList(newIntent)
                );
            } else if (BRActivityThreadNMR1.get(mainThread)._check_performNewIntents(null, null, false) != null) {
                BRActivityThreadNMR1.get(mainThread).performNewIntents(
                        token,
                        Collections.singletonList(newIntent),
                        true);
            } else if (BRActivityThreadQ.get(mainThread)._check_handleNewIntent(null, null) != null) {
                BRActivityThreadQ.get(mainThread).handleNewIntent(token, Collections.singletonList(newIntent));
            }
        });
    }

    @Override
    public void scheduleReceiver(ReceiverData data) throws RemoteException {
        if (!isInit()) {
            bindApplication();
        }
        mH.post(() -> {
            BroadcastReceiver mReceiver = null;
            Intent intent = data.intent;
            ActivityInfo activityInfo = data.activityInfo;
            BroadcastReceiver.PendingResult pendingResult = data.data.build();

            try {
                Context baseContext = mInitialApplication.getBaseContext();
                ClassLoader classLoader = baseContext.getClassLoader();
                intent.setExtrasClassLoader(classLoader);

                mReceiver = (BroadcastReceiver) classLoader.loadClass(activityInfo.name).newInstance();
                BRBroadcastReceiver.get(mReceiver).setPendingResult(pendingResult);
                mReceiver.onReceive(baseContext, intent);
                BroadcastReceiver.PendingResult finish = BRBroadcastReceiver.get(mReceiver).getPendingResult();
                if (finish != null) {
                    finish.finish();
                }
                BlackBoxCore.getBActivityManager().finishBroadcast(data.data);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                Slog.e(TAG,
                        "Error receiving broadcast " + intent
                                + " in " + mReceiver);
            }
        });
    }

    public static Activity getActivityByToken(IBinder token) {
        Map<IBinder, Object> iBinderObjectMap =
                BRActivityThread.get(BlackBoxCore.mainThread()).mActivities();
        return BRActivityThreadActivityClientRecord.get(iBinderObjectMap.get(token)).activity();
    }

    private void onBeforeCreateApplication(String packageName, String processName, Context context) {
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.beforeCreateApplication(packageName, processName, context, BActivityThread.getUserId());
        }
    }

    private void onBeforeApplicationOnCreate(String packageName, String processName, Application application) {
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.beforeApplicationOnCreate(packageName, processName, application, BActivityThread.getUserId());
        }
    }

    private void onAfterApplicationOnCreate(String packageName, String processName, Application application) {
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.afterApplicationOnCreate(packageName, processName, application, BActivityThread.getUserId());
        }
    }

    
    public static void ensureActivityContext(Activity activity) {
        if (activity == null) {
            return;
        }
        
        try {
            
            Context currentContext = activity.getBaseContext();
            if (currentContext != null) {
                Slog.d(TAG, "Activity already has context: " + activity.getClass().getName());
                return;
            }
            
            Slog.w(TAG, "Activity has null context, ensuring valid context: " + activity.getClass().getName());
            
            
            Context validContext = null;
            try {
                validContext = getApplication();
                if (validContext == null) {
                    validContext = BlackBoxCore.getContext();
                }
            } catch (Exception e) {
                Slog.w(TAG, "Could not get application context: " + e.getMessage());
                validContext = BlackBoxCore.getContext();
            }
            
            if (validContext != null) {
                
                try {
                    Context packageContext = validContext.createPackageContext(
                        activity.getPackageName(),
                        Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
                    );
                    
                    
                    java.lang.reflect.Method attachBaseContext = Activity.class.getDeclaredMethod("attachBaseContext", Context.class);
                    attachBaseContext.setAccessible(true);
                    attachBaseContext.invoke(activity, packageContext);
                    Slog.d(TAG, "Successfully attached package context to activity: " + activity.getClass().getName());
                } catch (Exception e) {
                    Slog.w(TAG, "Could not attach base context to activity: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error ensuring activity context: " + e.getMessage());
        }
    }

    
    public static void hookActivityThread() {
        try {
            
            Object activityThread = BlackBoxCore.mainThread();
            if (activityThread != null) {
                
                Instrumentation instrumentation = BRActivityThread.get(activityThread).mInstrumentation();
                if (instrumentation != null) {
                    Slog.d(TAG, "Found ActivityThread instrumentation, ensuring it's our AppInstrumentation");
                    
                    
                    if (!(instrumentation instanceof AppInstrumentation)) {
                        Slog.w(TAG, "ActivityThread instrumentation is not our AppInstrumentation, attempting to replace");
                        
                        
                        try {
                            AppInstrumentation appInstrumentation = AppInstrumentation.get();
                            appInstrumentation.injectHook();
                            Slog.d(TAG, "Successfully replaced ActivityThread instrumentation with AppInstrumentation");
                        } catch (Exception e) {
                            Slog.w(TAG, "Could not replace ActivityThread instrumentation: " + e.getMessage());
                        }
                    } else {
                        Slog.d(TAG, "ActivityThread instrumentation is already our AppInstrumentation");
                    }
                } else {
                    Slog.w(TAG, "ActivityThread instrumentation is null");
                }
            } else {
                Slog.w(TAG, "ActivityThread is null");
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error hooking ActivityThread: " + e.getMessage());
        }
    }

    public static class AppBindData {
        String processName;
        ApplicationInfo appInfo;
        List<ProviderInfo> providers;
        Object info;
    }
}
