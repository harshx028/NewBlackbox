package ref.android.app;

import android.os.IBinder;
import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BMethod;
import com.ncertbooks.reflect.annotation.BStaticMethod;

@BClassName("android.app.IApplicationThread")
public interface IApplicationThreadOreo {
    @BMethod
    void scheduleServiceArgs();

    @BClassName("android.app.IApplicationThread$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
