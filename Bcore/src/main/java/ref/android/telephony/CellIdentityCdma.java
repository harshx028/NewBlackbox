package ref.android.telephony;


import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BConstructor;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.telephony.CellIdentityCdma")
public interface CellIdentityCdma {
    @BConstructor
    CellIdentityCdma _new();

    @BField
    int mBasestationId();

    @BField
    int mNetworkId();

    @BField
    int mSystemId();
}
