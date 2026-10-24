// IBPackageInstallerService.aidl
package com.ncertbooks.engine.core.system.pm;

import com.ncertbooks.engine.core.system.pm.BPackageSettings;
import com.ncertbooks.engine.entity.pm.InstallOption;

// Declare any non-default types here with import statements

interface IBPackageInstallerService {
    int installPackageAsUser(in BPackageSettings ps, int userId);
    int uninstallPackageAsUser(in BPackageSettings ps, boolean removeApp, int userId);
    int clearPackage(in BPackageSettings ps, int userId);
    int updatePackage(in BPackageSettings ps);
}
