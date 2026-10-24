package ref.android.view;

import android.os.IBinder;
import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BStaticMethod;

@BClassName("android.view.autofill.IAutoFillManager")
public interface IAutoFillManager {
    @BClassName("android.view.autofill.IAutoFillManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
