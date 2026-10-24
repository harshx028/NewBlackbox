package ref.android.telephony;


import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BMethod;

@BClassName("android.telephony.SmsManager")
public interface SmsManager {
    @BMethod
    Boolean getAutoPersisting();

    @BMethod
    void setAutoPersisting(boolean boolean0);
}
