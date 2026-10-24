package black.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BStaticMethod;

@BClassName("android.os.UserManager")
public interface UserManager {
    @BClassName("android.os.IUserManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
