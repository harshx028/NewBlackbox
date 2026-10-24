package ref.android.app;

import java.io.File;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.app.ContextImpl")
public interface ContextImplKitkat {
    @BField
    Object mDisplayAdjustments();

    @BField
    File[] mExternalCacheDirs();

    @BField
    File[] mExternalFilesDirs();

    @BField
    String mOpPackageName();
}
