package black.android.app;

import android.content.Intent;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BConstructor;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.app.ServiceStartArgs")
public interface ServiceStartArgs {
    @BConstructor
    ServiceStartArgs _new(boolean boolean0, int int1, int int2, Intent Intent3);

    @BField
    Intent args();

    @BField
    int flags();

    @BField
    int startId();

    @BField
    boolean taskRemoved();
}
