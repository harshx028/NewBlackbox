package com.ncertbooks.engine.core.system.pm.installer;

import com.ncertbooks.engine.core.env.BEnvironment;
import com.ncertbooks.engine.core.system.pm.BPackageSettings;
import com.ncertbooks.engine.entity.pm.InstallOption;
import com.ncertbooks.engine.utils.FileUtils;


public class RemoveUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        
        FileUtils.deleteDir(BEnvironment.getDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getDeDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getExternalDataDir(packageName, userId));
        return 0;
    }
}
