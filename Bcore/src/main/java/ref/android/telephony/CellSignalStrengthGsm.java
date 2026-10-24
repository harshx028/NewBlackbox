package ref.android.telephony;


import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BConstructor;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.telephony.CellSignalStrengthGsm")
public interface CellSignalStrengthGsm {
    @BConstructor
    CellSignalStrengthGsm _new();

    @BField
    int mBitErrorRate();

    @BField
    int mSignalStrength();
}
