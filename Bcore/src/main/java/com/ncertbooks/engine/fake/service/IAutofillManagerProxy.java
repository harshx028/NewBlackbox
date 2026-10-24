package com.ncertbooks.engine.fake.service;

import android.content.ComponentName;

import java.lang.reflect.Method;

import ref.android.os.BRServiceManager;
import ref.android.view.BRIAutoFillManagerStub;
import com.ncertbooks.engine.BlackBoxCore;
import com.ncertbooks.engine.app.BActivityThread;
import com.ncertbooks.engine.fake.hook.BinderInvocationStub;
import com.ncertbooks.engine.fake.hook.MethodHook;
import com.ncertbooks.engine.fake.hook.ProxyMethod;
import com.ncertbooks.engine.proxy.ProxyManifest;


public class IAutofillManagerProxy extends BinderInvocationStub {
    public static final String TAG = "AutofillManagerStub";

    public IAutofillManagerProxy() {
        super(BRServiceManager.get().getService("autofill"));
    }

    @Override
    protected Object getWho() {
        return BRIAutoFillManagerStub.get().asInterface(BRServiceManager.get().getService("autofill"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("autofill");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("startSession")
    public static class StartSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null)
                        continue;
                    if (args[i] instanceof ComponentName) {
                        args[i] = new ComponentName(BlackBoxCore.getHostPkg(), ProxyManifest.getProxyActivity(BlackBoxCore.getAppPid()));
                    }
                }
            }
            return method.invoke(who, args);
        }
    }
}
