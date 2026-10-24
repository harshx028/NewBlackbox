package black.android.content;

import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BField;
import com.ncertbooks.reflect.annotation.BStaticField;

@BClassName("android.content.ClipboardManager")
public interface ClipboardManagerOreo {
    @BStaticField
    IInterface sService();

    @BField
    IInterface mService();
}
