package black.android.hardware.location;

import android.os.IBinder;
import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BStaticMethod;


@BClassName("android.hardware.location.IContextHubService")
public interface IContextHubService {

    @BClassName("android.hardware.location.IContextHubService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder iBinder);
    }
}
