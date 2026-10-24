package ref.com.android.internal.view.inputmethod;

import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.view.inputmethod.InputMethodManager")
public interface InputMethodManager {
    @BField
    IInterface mService();
}
