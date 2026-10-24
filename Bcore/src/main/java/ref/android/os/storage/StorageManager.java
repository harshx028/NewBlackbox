package ref.android.os.storage;

import android.os.storage.StorageVolume;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BStaticMethod;

@BClassName("android.os.storage.StorageManager")
public interface StorageManager {
    @BStaticMethod
    StorageVolume[] getVolumeList(int int0, int int1);
}
