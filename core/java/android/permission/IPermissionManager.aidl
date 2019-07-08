/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.permission;

import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.permission.IOnPermissionsChangeListener;

/**
 * Interface to communicate directly with the permission manager service.
 * @see PermissionManager
 * @hide
 */
interface IPermissionManager {
    String[] getAppOpPermissionPackages(String permName);

    ParceledListSlice getAllPermissionGroups(int flags);

    PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags);

    PermissionInfo getPermissionInfo(String permName, String packageName, int flags);

    ParceledListSlice queryPermissionsByGroup(String groupName, int flags);

    boolean addPermission(in PermissionInfo info, boolean async);

    void removePermission(String name);

    int getPermissionFlags(String permName, String packageName, int userId);

    void updatePermissionFlags(String permName, String packageName, int flagMask,
            int flagValues, boolean checkAdjustPolicyFlagPermission, int userId);

    void updatePermissionFlagsForAllApps(int flagMask, int flagValues, int userId);

    int checkPermission(String permName, String pkgName, int userId);

    int checkUidPermission(String permName, int uid);

    void addOnPermissionsChangeListener(in IOnPermissionsChangeListener listener);

    void removeOnPermissionsChangeListener(in IOnPermissionsChangeListener listener);

    List<String> getWhitelistedRestrictedPermissions(String packageName,
            int flags, int userId);

    boolean addWhitelistedRestrictedPermission(String packageName, String permName,
            int flags, int userId);

    boolean removeWhitelistedRestrictedPermission(String packageName, String permName,
            int flags, int userId);
}
