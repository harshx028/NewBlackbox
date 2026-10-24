package ref.android.location;


import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BMethod;

@BClassName("android.location.GpsStatus")
public interface GpsStatus {
    @BMethod
    void setStatus(int int0, int[] ints1, float[] floats2, float[] floats3, float[] floats4, int int5, int int6, int int7);
}
