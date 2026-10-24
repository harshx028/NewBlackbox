package ref.android.app;

import android.os.IBinder;
import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BStaticMethod;

@BClassName("android.app.IActivityTaskManager")
public interface IActivityTaskManager {
    @BClassName("android.app.IActivityTaskManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
