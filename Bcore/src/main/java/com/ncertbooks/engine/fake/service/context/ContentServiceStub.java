package com.ncertbooks.engine.fake.service.context;

import java.lang.reflect.Method;

import ref.android.content.BRIContentServiceStub;
import ref.android.os.BRServiceManager;
import com.ncertbooks.engine.fake.hook.BinderInvocationStub;
import com.ncertbooks.engine.fake.hook.MethodHook;
import com.ncertbooks.engine.fake.hook.ProxyMethod;


public class ContentServiceStub extends BinderInvocationStub {

    public ContentServiceStub() {
        super(BRServiceManager.get().getService("content"));
    }

    @Override
    protected Object getWho() {
        return BRIContentServiceStub.get().asInterface(BRServiceManager.get().getService("content"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("content");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("registerContentObserver")
    public static class RegisterContentObserver extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("notifyChange")
    public static class NotifyChange extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }
}
