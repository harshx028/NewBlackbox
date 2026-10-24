package com.ncertbooks.engine.core.system.pm.installer;

import com.ncertbooks.engine.core.system.pm.BPackageSettings;
import com.ncertbooks.engine.entity.pm.InstallOption;


public interface Executor {
    public static final String TAG = "InstallExecutor";

    int exec(BPackageSettings ps, InstallOption option, int userId);
}
