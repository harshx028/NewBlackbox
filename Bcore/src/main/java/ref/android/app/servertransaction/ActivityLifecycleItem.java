package ref.android.app.servertransaction;


import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BMethod;

@BClassName("android.app.servertransaction.ActivityLifecycleItem")
public interface ActivityLifecycleItem {
    @BMethod
    Integer getTargetState();
}
