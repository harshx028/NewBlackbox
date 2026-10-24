package ref.android.service.notification;


import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BField;

@BClassName("android.service.notification.StatusBarNotification")
public interface StatusBarNotification {
    @BField
    Integer id();

    @BField
    String opPkg();

    @BField
    String pkg();

    @BField
    String tag();
}
