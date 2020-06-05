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
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheFactory;
import org.xwiki.cache.CacheManager;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlan;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.job.Job;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.Request;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.test.annotation.AfterComponent;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AllComponents
public abstract class AbstractExtensionHandlerTest
{
    @InjectComponentManager
    protected MockitoComponentManager componentManager;

    protected LocalExtensionRepository localExtensionRepository;

    protected InstalledExtensionRepository installedExtensionRepository;

    protected JobExecutor jobExecutor;

    protected MemoryConfigurationSource memoryConfigurationSource;

    // We inject infinispan after components have been loaded to be sure to mock it only if
    // it's not available in all components.
    @AfterComponent
    public void afterComponent() throws Exception
    {
        if (!this.componentManager.hasComponent(CacheFactory.class, "infinispan")) {
            CacheFactory infinispan = this.componentManager.registerMockComponent(CacheFactory.class, "infinispan");
            when(infinispan.newCache(any())).thenReturn(mock(Cache.class));
        }
    }

    @BeforeEach
    public void setUp() throws Exception
    {
        this.jobExecutor = this.componentManager.getInstance(JobExecutor.class);
        this.localExtensionRepository = this.componentManager.getInstance(LocalExtensionRepository.class);
        this.installedExtensionRepository = this.componentManager.getInstance(InstalledExtensionRepository.class);

        this.componentManager.registerMockComponent(CacheManager.class);

        this.memoryConfigurationSource = this.componentManager.registerMemoryConfigurationSource();
    }

    protected ExtensionPlanNode getNode(ExtensionId id, Collection<ExtensionPlanNode> nodes)
    {
        for (ExtensionPlanNode node : nodes) {
            if (node.getAction().getExtension().getId().equals(id)) {
                return node;
            }
        }

        return null;
    }

    protected Job executeJob(String jobId, Request request, LogLevel failFrom) throws Throwable
    {
        Job installJob = this.jobExecutor.execute(jobId, request);

        installJob.join();

        if (installJob.getStatus().getError() != null) {
            throw installJob.getStatus().getError();
        }

        Optional<LogEvent> errorResult =
            installJob.getStatus().getLogTail().getLogEvents(failFrom).stream().findFirst();
        if (errorResult.isPresent()) {
            LogEvent error = errorResult.get();
            throw error.getThrowable() != null ? error.getThrowable() : new Exception(error.getFormattedMessage());
        }

        return installJob;
    }

    protected InstalledExtension install(ExtensionId extensionId) throws Throwable
    {
        return install(extensionId, true);
    }

    protected InstalledExtension install(ExtensionId extensionId, boolean rootModifications) throws Throwable
    {
        return install(extensionId, (String[]) null, rootModifications);
    }

    protected InstalledExtension install(ExtensionId extensionId, String[] namespaces) throws Throwable
    {
        return install(extensionId, namespaces, true);
    }

    protected InstalledExtension install(ExtensionId extensionId, String[] namespaces, boolean rootModifications)
        throws Throwable
    {
        return install(extensionId, namespaces, rootModifications, LogLevel.WARN);
    }

    protected InstalledExtension install(ExtensionId extensionId, String namespace) throws Throwable
    {
        return install(extensionId, namespace, true);
    }

    protected InstalledExtension install(ExtensionId extensionId, String namespace, boolean rootModifications)
        throws Throwable
    {
        return install(extensionId, namespace, rootModifications, LogLevel.WARN);
    }

    protected InstalledExtension install(ExtensionId extensionId, String namespace, LogLevel failFrom) throws Throwable
    {
        return install(extensionId, namespace, true, failFrom);
    }

    protected InstalledExtension install(ExtensionId extensionId, String namespace, boolean rootModifications,
        LogLevel failFrom) throws Throwable
    {
        return install(extensionId, namespace != null ? new String[] { namespace } : (String[]) null, rootModifications,
            failFrom);
    }

    protected InstalledExtension install(ExtensionId extensionId, String[] namespaces, LogLevel failFrom)
        throws Throwable
    {
        return install(extensionId, namespaces, true, failFrom);
    }

    protected InstalledExtension install(ExtensionId extensionId, String[] namespaces, boolean rootModifications,
        LogLevel failFrom) throws Throwable
    {
        install("install", extensionId, namespaces, rootModifications, failFrom);

        return this.installedExtensionRepository.resolve(extensionId);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId) throws Throwable
    {
        return installPlan(extensionId, true);
    }

    protected ExtensionPlan installPlan(Collection<ExtensionId> extensionIds) throws Throwable
    {
        return installPlan(extensionIds, true);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, boolean rootModifications) throws Throwable
    {
        return installPlan(extensionId, (String[]) null, rootModifications);
    }

    protected ExtensionPlan installPlan(Collection<ExtensionId> extensionIds, boolean rootModifications)
        throws Throwable
    {
        return installPlan(extensionIds, (String[]) null, rootModifications);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String[] namespaces) throws Throwable
    {
        return installPlan(extensionId, namespaces, true);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String[] namespaces, boolean rootModifications)
        throws Throwable
    {
        return installPlan(extensionId, namespaces, rootModifications, LogLevel.WARN);
    }

    protected ExtensionPlan installPlan(Collection<ExtensionId> extensionIds, String[] namespaces,
        boolean rootModifications) throws Throwable
    {
        return installPlan(extensionIds, namespaces, rootModifications, LogLevel.WARN);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String namespace) throws Throwable
    {
        return installPlan(extensionId, namespace, true);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String namespace, boolean rootModifications)
        throws Throwable
    {
        return installPlan(extensionId, namespace != null ? new String[] { namespace } : null, rootModifications,
            LogLevel.WARN);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String[] namespaces, LogLevel failFrom)
        throws Throwable
    {
        return installPlan(extensionId, namespaces, true, failFrom);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String[] namespaces, boolean rootModifications,
        LogLevel failFrom) throws Throwable
    {
        return installPlan(Arrays.asList(extensionId), namespaces, rootModifications, failFrom);
    }

    protected ExtensionPlan installPlan(Collection<ExtensionId> extensionIds, String[] namespaces,
        boolean rootModifications, LogLevel failFrom) throws Throwable
    {
        InstallRequest installRequest = createInstallRequest(extensionIds, namespaces, rootModifications);

        return installPlan(installRequest, failFrom);
    }

    protected ExtensionPlan installPlan(InstallRequest installRequest) throws Throwable
    {
        return installPlan(installRequest, LogLevel.WARN);
    }

    protected ExtensionPlan installPlan(InstallRequest installRequest, LogLevel failFrom) throws Throwable
    {
        Job installJob = executeJob("installplan", installRequest, failFrom);

        return (ExtensionPlan) installJob.getStatus();
    }

    protected Job install(String jobId, ExtensionId extensionId, String[] namespaces, LogLevel failFrom)
        throws Throwable
    {
        return install(jobId, extensionId, namespaces, true, failFrom);
    }

    protected Job install(String jobId, ExtensionId extensionId, String[] namespaces, boolean rootModifications,
        LogLevel failFrom) throws Throwable
    {
        InstallRequest installRequest = createInstallRequest(extensionId, namespaces, rootModifications);

        return executeJob(jobId, installRequest, failFrom);
    }

    protected InstallRequest createInstallRequest(ExtensionId extensionId)
    {
        return createInstallRequest(extensionId, null, true);
    }

    protected InstallRequest createInstallRequest(ExtensionId extensionId, String[] namespaces,
        boolean rootModifications)
    {
        return createInstallRequest(Arrays.asList(extensionId), namespaces, rootModifications);
    }

    protected InstallRequest createInstallRequest(Collection<ExtensionId> extensionIds)
    {
        return createInstallRequest(extensionIds);
    }

    protected InstallRequest createInstallRequest(Collection<ExtensionId> extensionIds, String[] namespaces,
        boolean rootModifications)
    {
        InstallRequest installRequest = new InstallRequest();
        extensionIds.stream().forEach(id -> installRequest.addExtension(id));
        if (namespaces != null) {
            for (String namespace : namespaces) {
                installRequest.addNamespace(namespace);
            }
        }
        installRequest.setRootModificationsAllowed(rootModifications);
        installRequest.setVerbose(false);

        return installRequest;
    }

    protected LocalExtension uninstall(ExtensionId extensionId) throws Throwable
    {
        return uninstall(extensionId, (Iterable<String>) null);
    }

    protected LocalExtension uninstall(ExtensionId extensionId, String namespace) throws Throwable
    {
        return uninstall(extensionId, namespace, LogLevel.WARN);
    }

    protected LocalExtension uninstall(ExtensionId extensionId, Iterable<String> namespaces) throws Throwable
    {
        return uninstall(extensionId, namespaces, LogLevel.WARN);
    }

    protected LocalExtension uninstall(ExtensionId extensionId, LogLevel failFrom) throws Throwable
    {
        return uninstall(extensionId, (Iterable<String>) null, failFrom);
    }

    protected LocalExtension uninstall(ExtensionId extensionId, String namespace, LogLevel failFrom) throws Throwable
    {
        return uninstall(extensionId, namespace != null ? Arrays.asList(namespace) : null, failFrom);
    }

    protected LocalExtension uninstall(ExtensionId extensionId, Iterable<String> namespaces, LogLevel failFrom)
        throws Throwable
    {
        uninstall("uninstall", extensionId, namespaces, failFrom);

        return this.localExtensionRepository.resolve(extensionId);
    }

    protected DefaultExtensionPlan<UninstallRequest> uninstallPlan(ExtensionId extensionId, String namespace,
        LogLevel failFrom) throws Throwable
    {
        Job uninstallJob = uninstall("installplan", extensionId, namespace, failFrom);

        return (DefaultExtensionPlan<UninstallRequest>) uninstallJob.getStatus();
    }

    protected Job uninstall(String jobId, ExtensionId extensionId, String namespace, LogLevel failFrom) throws Throwable
    {
        return uninstall(jobId, extensionId, namespace != null ? Arrays.asList(namespace) : null, failFrom);
    }

    protected Job uninstall(String jobId, ExtensionId extensionId, Iterable<String> namespaces, LogLevel failFrom)
        throws Throwable
    {
        UninstallRequest uninstallRequest = new UninstallRequest();
        uninstallRequest.addExtension(extensionId);
        if (namespaces != null) {
            namespaces.forEach(uninstallRequest::addNamespace);
        }

        return executeJob(jobId, uninstallRequest, failFrom);
    }

    protected ExtensionPlan upgradePlan() throws Throwable
    {
        return upgradePlan((String) null);
    }

    protected ExtensionPlan upgradePlan(String namespace) throws Throwable
    {
        return upgradePlan(namespace, (Collection<ExtensionId>) null);
    }

    protected ExtensionPlan upgradePlan(String namespace, Collection<ExtensionId> excludedExtensions) throws Throwable
    {
        return upgradePlan(namespace, excludedExtensions, LogLevel.WARN);
    }

    protected ExtensionPlan upgradePlan(String namespace, LogLevel failFrom) throws Throwable
    {
        return upgradePlan(namespace, null, failFrom);
    }

    protected ExtensionPlan upgradePlan(String namespace, Collection<ExtensionId> excludedExtensions, LogLevel failFrom)
        throws Throwable
    {
        InstallRequest installRequest = new InstallRequest();
        if (namespace != null) {
            installRequest.addNamespace(namespace);
        }
        if (excludedExtensions != null) {
            installRequest.getExcludedExtensions().addAll(excludedExtensions);
        }

        return (ExtensionPlan) executeJob("upgradeplan", installRequest, failFrom).getStatus();
    }

    protected ExtensionPlan upgradePlan(InstallRequest installRequest) throws Throwable
    {
        return upgradePlan(installRequest, LogLevel.WARN);
    }

    protected ExtensionPlan upgradePlan(InstallRequest installRequest, LogLevel failFrom) throws Throwable
    {
        return (ExtensionPlan) executeJob("upgradeplan", installRequest, failFrom).getStatus();
    }
}
