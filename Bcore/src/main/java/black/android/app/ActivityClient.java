package black.android.app;

import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BField;
import com.ncertbooks.reflect.annotation.BStaticMethod;


@BClassName("android.app.ActivityClient")
public interface ActivityClient {
    @BField
    Object INTERFACE_SINGLETON();

    @BStaticMethod
    Object getInstance();

    @BStaticMethod
    Object getActivityClientController();

    @BStaticMethod
    Object setActivityClientController(Object iInterface);

    @BClassName("android.app.ActivityClient$ActivityClientControllerSingleton")
    interface ActivityClientControllerSingleton {
        @BField
        IInterface mKnownInstance();
    }
}
