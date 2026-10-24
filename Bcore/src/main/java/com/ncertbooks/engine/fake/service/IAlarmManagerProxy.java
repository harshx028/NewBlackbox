package com.ncertbooks.engine.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import ref.android.app.BRIAlarmManagerStub;
import ref.android.os.BRServiceManager;
import com.ncertbooks.engine.fake.hook.BinderInvocationStub;
import com.ncertbooks.engine.fake.hook.MethodHook;
import com.ncertbooks.engine.fake.hook.ProxyMethod;


public class IAlarmManagerProxy extends BinderInvocationStub {

    public IAlarmManagerProxy() {
        super(BRServiceManager.get().getService(Context.ALARM_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIAlarmManagerStub.get().asInterface(BRServiceManager.get().getService(Context.ALARM_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.ALARM_SERVICE);
    }

    @ProxyMethod("set")
    public static class Set extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
