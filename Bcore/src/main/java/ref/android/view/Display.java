package ref.android.view;

import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BStaticField;

@BClassName("android.view.Display")
public interface Display {
    @BStaticField
    IInterface sWindowManager();
}
