package com.ncertbooks.engine.fake.service;


import ref.android.hardware.location.BRIContextHubServiceStub;
import ref.android.os.BRServiceManager;
import com.ncertbooks.engine.fake.hook.BinderInvocationStub;
import com.ncertbooks.engine.fake.service.base.ValueMethodProxy;
import com.ncertbooks.engine.utils.compat.BuildCompat;


public class IContextHubServiceProxy extends BinderInvocationStub {

    public IContextHubServiceProxy() {
        super(BRServiceManager.get().getService(getServiceName()));
    }

    private static String getServiceName() {
        return BuildCompat.isOreo() ? "contexthub" : "contexthub_service";
    }

    @Override
    protected Object getWho() {
        return BRIContextHubServiceStub.get().asInterface(BRServiceManager.get().getService(getServiceName()));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(getServiceName());
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("registerCallback", 0));
        addMethodHook(new ValueMethodProxy("getContextHubInfo", null));
        addMethodHook(new ValueMethodProxy("getContextHubHandles",new int[]{}));
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
