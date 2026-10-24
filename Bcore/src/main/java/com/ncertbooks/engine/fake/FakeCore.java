package com.ncertbooks.engine.fake;

import com.ncertbooks.jni.ReflectCore;


public class FakeCore {
    public static void init() {
        ReflectCore.set(android.app.ActivityThread.class);
    }
}
