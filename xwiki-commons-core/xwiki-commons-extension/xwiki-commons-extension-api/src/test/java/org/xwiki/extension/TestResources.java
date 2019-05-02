/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension;

import org.xwiki.extension.repository.InstalledExtensionRepository;

public class TestResources
{
    // Core

    public static final ExtensionId CORE_ID = new ExtensionId("coreextension", "version");

    // Installed

    public static final ExtensionId INSTALLED_ID = new ExtensionId("installedextension", "1.0");

    public static final ExtensionId INSTALLED_INVALID_ID = new ExtensionId("installedextensioninvalid", "version");

    public static final ExtensionId INSTALLED_DEPENDENCY_ID = new ExtensionId("installedextensiondependency", "1.0");

    public static final ExtensionId INSTALLED_ONNAMESPACE_ID = new ExtensionId("installedonnamespace", "version");

    public static final ExtensionId INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID =
        new ExtensionId("installedonnemspacewithrootdependency", "version");

    public static final ExtensionId INSTALLED_WITHFEATUREONNAMESPACE_ID =
        new ExtensionId("installedwithfeatureonnamespace", "version");

    public static final ExtensionId INSTALLED_WITHFEATUREASDEPENDENCY_ID =
        new ExtensionId("installedwithfeatureasdependency", "version");

    public static final ExtensionId INSTALLED_WITHMISSINDEPENDENCY_ID =
        new ExtensionId("installedwithmissingdependency", "version");

    public InstalledExtension installed;

    public InstalledExtension installedDependency;

    public InstalledExtension installedwithfeatureasdependency;

    public InstalledExtension installedonnemspacewithrootdependency;

    // Remote

    public static final ExtensionId REMOTE_SIMPLE_ID = new ExtensionId("rsimple", "version");

    public static final ExtensionId REMOTE_WITHRDEPENDENCY_ID = new ExtensionId("rwithrdependency", "version");

    public static final ExtensionId REMOTE_WITHRDEPENDENCYFEATURE_ID =
        new ExtensionId("rwithrdependencyfeature", "version");

    public static final ExtensionId REMOTE_WITHCDEPENDENCY_ID = new ExtensionId("rwithcdependency", "version");

    public static final ExtensionId REMOTE_WITHLDEPENDENCY_ID = new ExtensionId("rwithldependency", "version");

    public static final ExtensionId REMOTE_WITHRANDCDEPENDENCIES_ID =
        new ExtensionId("rwithrandcdependencies", "version");

    public static final ExtensionId REMOTE_WITHRMISSINGDEPENDENCY_ID =
        new ExtensionId("rwithmissingdependency", "version");

    public static final ExtensionId REMOTE_WITHRMISSINGOPTIONALDEPENDENCY_ID =
        new ExtensionId("rwithmissingoptionaldependency", "version");

    public static final ExtensionId REMOTE_MISSINGDEPENDENCY_ID = new ExtensionId("missingdependency", "version");

    public static final ExtensionId REMOTE_UPGRADE10_ID = new ExtensionId("upgrade", "1.0");

    public static final ExtensionId REMOTE_UPGRADE20_ID = new ExtensionId("upgrade", "2.0");

    public static final ExtensionId REMOTE_UPGRADEFEATURE20_ID = new ExtensionId("upgrade-feature", "2.0");

    public static final ExtensionId REMOTE_UPGRADEWITHFEATURE10_ID = new ExtensionId("upgrade-withfeature", "1.0");

    public static final ExtensionId REMOTE_UPGRADEWITHDEPENDENCY10_ID =
        new ExtensionId("upgrade-withdependency", "1.0");

    public static final ExtensionId REMOTE_UPGRADEWITHDEPENDENCY20_ID =
        new ExtensionId("upgrade-withdependency", "2.0");

    public static final ExtensionId REMOTE_OTHERUPGRADEWITHDEPENDENCY20_ID =
        new ExtensionId("other-upgrade-withdependency", "2.0");

    public static final ExtensionId REMOTE_ROOTEXTENSION10_ID = new ExtensionId("rootextension", "1.0");

    public static final ExtensionId REMOTE_ROOTEXTENSION20_ID = new ExtensionId("rootextension", "2.0");

    public static final ExtensionId REMOTE_UNSUPPORTED_ID = new ExtensionId("unsupported", "version");

    public static final ExtensionId REMOTE_NOTINSTALLED_ID = new ExtensionId("notinstalledextension", "2.0");

    public static final ExtensionId REMOTE_NOTINSTALLED_DEPENDENCY_ID =
        new ExtensionId("notinstalledextensiondependency", "2.0");

    public static final ExtensionId REMOTE_WITH_ROOT_DEPENDENY10_ID = new ExtensionId("rwithrootdependency", "1.0");

    public static final ExtensionId REMOTE_WITH_ROOT_DEPENDENY20_ID = new ExtensionId("rwithrootdependency", "2.0");

    public static final ExtensionId REMOTE_WITH_MANAGED_DEPENDENY_ID =
        new ExtensionId("rwithmanageddependency", "version");

    public static final ExtensionId REMOTE_WITH_MANAGED_TRANSITIVEDEPENDENY_ID =
        new ExtensionId("rwithmanagedtransitivedependency", "version");

    public static final ExtensionId REMOTE_WITH_OVERWRITTEN_MANAGED_DEPENDENY_ID =
        new ExtensionId("rwithoverwrittenmanageddependency", "version");

    public static final ExtensionId REMOTE_CROSSDEPENDENCY1 = new ExtensionId("crossdependency1", "version");

    public static final ExtensionId REMOTE_CROSSDEPENDENCY2 = new ExtensionId("crossdependency2", "version");

    // Methods

    public void init(InstalledExtensionRepository installedExtensionRepository) throws ResolveException
    {
        this.installed = installedExtensionRepository.resolve(INSTALLED_ID);
        this.installedDependency = installedExtensionRepository.resolve(INSTALLED_DEPENDENCY_ID);
        this.installedwithfeatureasdependency =
            installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_WITHFEATUREASDEPENDENCY_ID);
        this.installedonnemspacewithrootdependency = installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID);
    }
}
