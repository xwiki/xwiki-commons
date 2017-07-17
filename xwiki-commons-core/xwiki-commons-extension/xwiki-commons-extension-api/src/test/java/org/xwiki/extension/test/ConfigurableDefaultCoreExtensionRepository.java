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
package org.xwiki.extension.test;

import java.util.Arrays;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.extension.version.Version;

@Component(staticRegistration = false)
@Singleton
public class ConfigurableDefaultCoreExtensionRepository extends DefaultCoreExtensionRepository
{
    public class ConfigurableEnvironmentExtension extends DefaultCoreExtension
    {
        public ConfigurableEnvironmentExtension(DefaultCoreExtensionRepository repository)
        {
            super(repository, null, new ExtensionId("environment", "version"), "type");
        }
    }

    private ConfigurableEnvironmentExtension environmentExtension;

    public ConfigurableDefaultCoreExtensionRepository()
    {
        this.environmentExtension = new ConfigurableEnvironmentExtension(this);
    }

    @Override
    public CoreExtension getEnvironmentExtension()
    {
        return getConfigurableEnvironmentExtension();
    }

    public ConfigurableEnvironmentExtension getConfigurableEnvironmentExtension()
    {
        return this.environmentExtension;
    }

    @Override
    public void addExtension(DefaultCoreExtension extension)
    {
        super.addExtension(extension);
    }

    public void addExtensions(String id, Version version, ExtensionId... features)
    {
        DefaultCoreExtension coreExtension =
            new DefaultCoreExtension(null, null, new ExtensionId(id, version), "unknown");

        if (features.length > 0) {
            coreExtension.setExtensionFeatures(Arrays.asList(features));
        }

        this.extensions.put(id, coreExtension);

        for (ExtensionId feature : coreExtension.getExtensionFeatures()) {
            this.extensions.put(feature.getId(), coreExtension);
        }
    }
}
