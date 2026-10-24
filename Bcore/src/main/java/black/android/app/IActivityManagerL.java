package black.android.app;

import android.content.Intent;
import android.os.IBinder;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BMethod;

@BClassName("android.app.IActivityManager")
public interface IActivityManagerL {
    @BMethod
    Boolean finishActivity(IBinder IBinder0, int int1, Intent Intent2, boolean boolean3);
}
