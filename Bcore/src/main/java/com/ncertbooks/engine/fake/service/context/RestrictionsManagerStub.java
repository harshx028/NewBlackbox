package com.ncertbooks.engine.fake.service.context;

import android.content.Context;

import java.lang.reflect.Method;

import ref.android.content.BRIRestrictionsManagerStub;
import ref.android.os.BRServiceManager;
import com.ncertbooks.engine.BlackBoxCore;
import com.ncertbooks.engine.fake.hook.BinderInvocationStub;
import com.ncertbooks.engine.fake.hook.MethodHook;
import com.ncertbooks.engine.fake.hook.ProxyMethod;


public class RestrictionsManagerStub extends BinderInvocationStub {

    public RestrictionsManagerStub() {
        super(BRServiceManager.get().getService(Context.RESTRICTIONS_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIRestrictionsManagerStub.get().asInterface(BRServiceManager.get().getService(Context.RESTRICTIONS_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.RESTRICTIONS_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getApplicationRestrictions")
    public static class GetApplicationRestrictions extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            args[0] = BlackBoxCore.getHostPkg();
            return method.invoke(who, args);
        }
    }
}
