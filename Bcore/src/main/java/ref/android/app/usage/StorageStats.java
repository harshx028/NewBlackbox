package ref.android.app.usage;


import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BConstructor;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.app.usage.StorageStats")
public interface StorageStats {
    @BConstructor
    StorageStats _new();

    @BField
    long cacheBytes();

    @BField
    long codeBytes();

    @BField
    long dataBytes();
}
