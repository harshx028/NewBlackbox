package black.android.content;

import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BStaticField;
import com.ncertbooks.reflect.annotation.BStaticMethod;

@BClassName("android.content.ClipboardManager")
public interface ClipboardManager {
    @BStaticField
    IInterface sService();

    @BStaticMethod
    IInterface getService();
}
