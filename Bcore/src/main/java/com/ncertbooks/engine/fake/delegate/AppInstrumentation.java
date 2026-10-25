package com.ncertbooks.engine.fake.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.InMemoryDexClassLoader;
import ref.android.app.BRActivity;
import ref.android.app.BRActivityThread;
import com.ncertbooks.engine.BlackBoxCore;
import com.ncertbooks.engine.app.BActivityThread;
import com.ncertbooks.engine.fake.hook.HookManager;
import com.ncertbooks.engine.fake.hook.IInjectHook;
import com.ncertbooks.engine.fake.service.HCallbackProxy;
import com.ncertbooks.engine.fake.service.IActivityClientProxy;
import com.ncertbooks.engine.utils.HackAppUtils;
import com.ncertbooks.engine.utils.compat.ActivityCompat;
import com.ncertbooks.engine.utils.compat.ActivityManagerCompat;
import com.ncertbooks.engine.utils.compat.ContextCompat;

public final class AppInstrumentation extends BaseInstrumentationDelegate implements IInjectHook {

    private static final String TAG = AppInstrumentation.class.getSimpleName();

    private static AppInstrumentation sAppInstrumentation;

    // Cached combined ClassLoader built from Helium asset-embedded DEX files.
    // Helium splits (e.g. split_heliumcore.apk) do NOT have classes.dex at root;
    // they store it at assets/<splitname>/classes.dex. Standard DexPathList can't
    // find them, so we load them into memory via InMemoryDexClassLoader (API 26+).
    // Android 10+ also blocks DexClassLoader from writable paths, making in-memory
    // loading the only viable approach.
    private static volatile ClassLoader sHeliumLoader;
    private static final Object sHeliumLock = new Object();

    public static AppInstrumentation get() {
        if (sAppInstrumentation == null) {
            synchronized (AppInstrumentation.class) {
                if (sAppInstrumentation == null) {
                    sAppInstrumentation = new AppInstrumentation();
                }
            }
        }
        return sAppInstrumentation;
    }

    public AppInstrumentation() {
    }

    @Override
    public void injectHook() {
        try {
            Instrumentation mInstrumentation = getCurrInstrumentation();
            if (mInstrumentation == this || checkInstrumentation(mInstrumentation))
                return;
            mBaseInstrumentation = (Instrumentation) mInstrumentation;
            BRActivityThread.get(BlackBoxCore.mainThread())._set_mInstrumentation(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instrumentation getCurrInstrumentation() {
        Object currentActivityThread = BlackBoxCore.mainThread();
        return BRActivityThread.get(currentActivityThread).mInstrumentation();
    }

    @Override
    public boolean isBadEnv() {
        return !checkInstrumentation(getCurrInstrumentation());
    }

    private boolean checkInstrumentation(Instrumentation instrumentation) {
        if (instrumentation instanceof AppInstrumentation) {
            return true;
        }
        Class<?> clazz = instrumentation.getClass();
        if (Instrumentation.class.equals(clazz)) {
            return false;
        }
        do {
            assert clazz != null;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Instrumentation.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        Object obj = field.get(instrumentation);
                        if ((obj instanceof AppInstrumentation)) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Instrumentation.class.equals(clazz));
        return false;
    }

    private void checkHCallback() {
        HookManager.get().checkEnv(HCallbackProxy.class);
    }

    private void checkActivity(Activity activity) {
        Log.d(TAG, "callActivityOnCreate: " + activity.getClass().getName());
        HackAppUtils.enableQQLogOutput(activity.getPackageName(), activity.getClassLoader());
        checkHCallback();
        HookManager.get().checkEnv(IActivityClientProxy.class);
        ActivityInfo info = BRActivity.get(activity).mActivityInfo();
        ContextCompat.fix(activity);
        ActivityCompat.fix(activity);
        if (info.theme != 0) {
            activity.getTheme().applyStyle(info.theme, true);
        }
        ActivityManagerCompat.setActivityOrientation(activity, info.screenOrientation);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ContextCompat.fix(context);

        return super.newApplication(cl, className, context);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        checkActivity(activity);
        super.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        checkActivity(activity);
        super.callActivityOnCreate(activity, icicle);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        checkHCallback();
        super.callApplicationOnCreate(app);
    }

    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            return super.newActivity(cl, className, intent);
        } catch (ClassNotFoundException e) {
            try {
                return mBaseInstrumentation.newActivity(cl, className, intent);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e2) {
                // Try Thread context ClassLoader — frameworks like Instagram's Helium register
                // their module ClassLoader here after Application.onCreate().
                ClassLoader ctxCl = Thread.currentThread().getContextClassLoader();
                if (ctxCl != null && ctxCl != cl) {
                    try {
                        Class<?> clazz = ctxCl.loadClass(className);
                        return (Activity) clazz.getDeclaredConstructor().newInstance();
                    } catch (Exception ignored) {
                        Log.w(TAG, "Context ClassLoader fallback failed for " + className);
                    }
                }
                // Try direct classloader
                try {
                    Class<?> clazz = cl.loadClass(className);
                    return (Activity) clazz.getDeclaredConstructor().newInstance();
                } catch (Exception ignored) {
                    Log.w(TAG, "Direct ClassLoader fallback failed for " + className);
                }
                // Helium fallback: load asset-embedded DEX from split APKs into memory.
                // Instagram's Helium stores classes at assets/<splitname>/classes.dex, not root.
                // Android 10+ blocks DexClassLoader from writable paths, so we use
                // InMemoryDexClassLoader to avoid writing to disk entirely.
                Activity heliumActivity = tryLoadFromHeliumAssets(cl, className);
                if (heliumActivity != null) {
                    return heliumActivity;
                }
                // InternalLauncher is a Helium virtual class — not present in any real DEX.
                // Helium maps it at runtime via a custom ClassLoader that only exists after
                // Application.onCreate(). When p0 restarts with InternalLauncher as the top
                // activity (after a prior crash), redirect to the real sign-in screen so the
                // process can initialize cleanly instead of crash-looping.
                if (className.contains("InternalLauncher")) {
                    Log.d(TAG, "InternalLauncher redirect → BloksSignedOutFragmentActivity");
                    try {
                        return super.newActivity(cl, "com.instagram.nux.activity.BloksSignedOutFragmentActivity", intent);
                    } catch (Exception ignored) {
                        Log.w(TAG, "BloksSignedOutFragmentActivity redirect failed, trying MainTabActivity");
                        try {
                            return super.newActivity(cl, "com.instagram.mainactivity.MainTabActivity", intent);
                        } catch (Exception ignored2) {}
                    }
                }
                throw new ClassNotFoundException(className, e2);
            }
        }
    }

    private Activity tryLoadFromHeliumAssets(ClassLoader cl, String className) {
        // InMemoryDexClassLoader requires API 26+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;

        // Fast path: use already-built combined Helium loader
        if (sHeliumLoader != null) {
            try {
                return instantiateActivity(sHeliumLoader, className);
            } catch (Exception ignored) {}
        }

        synchronized (sHeliumLock) {
            if (sHeliumLoader != null) {
                try {
                    return instantiateActivity(sHeliumLoader, className);
                } catch (Exception ignored) {}
            }

            try {
                List<ByteBuffer> dexBuffers = new ArrayList<>();
                List<File> splitApks = getSplitApksFromClassLoader(cl);

                for (File splitApk : splitApks) {
                    String apkName = splitApk.getName();
                    if (!apkName.startsWith("split_")) continue;
                    int dot = apkName.lastIndexOf('.');
                    String splitName = apkName.substring(6, dot > 6 ? dot : apkName.length());
                    String assetEntry = "assets/" + splitName + "/classes.dex";

                    try (ZipFile zip = new ZipFile(splitApk)) {
                        ZipEntry entry = zip.getEntry(assetEntry);
                        if (entry == null) continue; // not a Helium-style split

                        byte[] bytes = readAllBytes(zip.getInputStream(entry));
                        dexBuffers.add(ByteBuffer.wrap(bytes));
                        Log.d(TAG, "Helium DEX loaded: " + assetEntry + " (" + bytes.length + " bytes)");
                    } catch (IOException e) {
                        Log.w(TAG, "Could not read Helium DEX from " + apkName + ": " + e.getMessage());
                    }
                }

                if (dexBuffers.isEmpty()) return null;

                ByteBuffer[] buffers = dexBuffers.toArray(new ByteBuffer[0]);
                sHeliumLoader = new InMemoryDexClassLoader(buffers, cl);
                Log.d(TAG, "InMemoryDexClassLoader built with " + buffers.length + " Helium DEX(es)");

                try {
                    return instantiateActivity(sHeliumLoader, className);
                } catch (Exception e) {
                    Log.w(TAG, "Helium InMemoryDexClassLoader can't find: " + className);
                    return null;
                }
            } catch (Exception e) {
                Log.w(TAG, "tryLoadFromHeliumAssets error: " + e.getMessage());
                return null;
            }
        }
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) baos.write(buf, 0, n);
            return baos.toByteArray();
        }
    }

    private Activity instantiateActivity(ClassLoader loader, String className) throws Exception {
        Class<?> clazz = loader.loadClass(className);
        return (Activity) clazz.getDeclaredConstructor().newInstance();
    }

    private List<File> getSplitApksFromClassLoader(ClassLoader cl) {
        List<File> result = new ArrayList<>();
        try {
            // BaseDexClassLoader.pathList -> DexPathList.dexElements -> Element.path
            Class<?> bdcl = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = bdcl.getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(cl);
            if (pathList == null) return result;

            Field elementsField = pathList.getClass().getDeclaredField("dexElements");
            elementsField.setAccessible(true);
            Object[] elements = (Object[]) elementsField.get(pathList);
            if (elements == null) return result;

            for (Object element : elements) {
                File f = getFileFromElement(element);
                if (f != null && f.exists()) result.add(f);
            }
        } catch (Exception e) {
            Log.w(TAG, "getSplitApksFromClassLoader reflection failed: " + e.getMessage());
        }
        return result;
    }

    private File getFileFromElement(Object element) {
        for (String name : new String[]{"path", "zip", "file", "dexFile"}) {
            try {
                Field f = element.getClass().getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(element);
                if (val instanceof File) return (File) val;
                if (val instanceof String) return new File((String) val);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
