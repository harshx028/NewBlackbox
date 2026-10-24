package black.android.content.pm;

import android.content.pm.PackageParser.Package;

import java.io.File;

import com.ncertbooks.reflect.annotation.BClassName;
import com.ncertbooks.reflect.annotation.BConstructor;
import com.ncertbooks.reflect.annotation.BMethod;

@BClassName("android.content.pm.PackageParser")
public interface PackageParserLollipop22 {
    @BConstructor
    android.content.pm.PackageParser _new();
















    @BMethod
    void collectCertificates(Package p, int flags);

    @BMethod
    Package parsePackage(File File0, int int1);
}
