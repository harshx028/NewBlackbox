package ref.android.os;

import android.os.IInterface;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.os.DropBoxManager")
public interface DropBoxManager {
    @BField
    IInterface mService();
}
