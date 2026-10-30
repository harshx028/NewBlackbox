package com.ncertbooks.engine.fake.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import java.lang.reflect.Field;

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

    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            return super.newActivity(cl, className, intent);
        } catch (ClassNotFoundException e) {
            // Try the base instrumentation first (handles framework-wrapped classloaders).
            try {
                return mBaseInstrumentation.newActivity(cl, className, intent);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e2) {
                // Try the thread context ClassLoader — module frameworks (e.g. Helium) register
                // their classloader here after Application.onCreate().
                ClassLoader ctxCl = Thread.currentThread().getContextClassLoader();
                if (ctxCl != null && ctxCl != cl) {
                    try {
                        return (Activity) ctxCl.loadClass(className).getDeclaredConstructor().newInstance();
                    } catch (Exception ignored) {}
                }
                // InternalLauncher is a Helium virtual class — never in any real DEX.
                // Redirect to the real sign-in screen so the process does not crash-loop
                // when the back-stack is restored before the Helium ClassLoader is ready.
                if (className.contains("InternalLauncher")) {
                    Log.d(TAG, "InternalLauncher redirect -> BloksSignedOutFragmentActivity");
                    try {
                        return super.newActivity(cl, "com.instagram.nux.activity.BloksSignedOutFragmentActivity", intent);
                    } catch (Exception ignored) {
                        try {
                            return super.newActivity(cl, "com.instagram.mainactivity.MainTabActivity", intent);
                        } catch (Exception ignored2) {}
                    }
                }
                throw new ClassNotFoundException(className, e2);
            }
        }
    }
}
