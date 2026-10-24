package com.ncertbooks.engine.core.system.pm.installer;

import com.ncertbooks.engine.core.env.BEnvironment;
import com.ncertbooks.engine.core.system.pm.BPackageSettings;
import com.ncertbooks.engine.entity.pm.InstallOption;
import com.ncertbooks.engine.utils.FileUtils;


public class CreatePackageExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));

        
        FileUtils.mkdirs(BEnvironment.getAppDir(ps.pkg.packageName));
        FileUtils.mkdirs(BEnvironment.getAppLibDir(ps.pkg.packageName));
        return 0;
    }
}
