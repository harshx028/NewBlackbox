package ref.android.app;

import android.content.Intent;
import android.os.IBinder;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BConstructor;
import com.ncertbooks.reflect.annotation.BMethod;

@BClassName("android.app.PendingIntent")
public interface PendingIntentJBMR2 {
    @BConstructor
    PendingIntentJBMR2 _new(IBinder IBinder0);

    @BMethod
    Intent getIntent();
}
