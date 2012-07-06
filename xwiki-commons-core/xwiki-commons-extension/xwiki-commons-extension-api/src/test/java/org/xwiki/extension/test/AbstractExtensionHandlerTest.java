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

import java.util.List;

import org.junit.Before;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlan;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.job.Job;
import org.xwiki.job.JobManager;
import org.xwiki.job.Request;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.test.AbstractComponentTestCase;

public abstract class AbstractExtensionHandlerTest extends AbstractComponentTestCase
{
    protected LocalExtensionRepository localExtensionRepository;

    protected InstalledExtensionRepository installedExtensionRepository;

    protected RepositoryUtil repositoryUtil;

    protected JobManager jobManager;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        beforeRepositoryUtil();

        this.repositoryUtil = new RepositoryUtil(getComponentManager(), getMockery());
        this.repositoryUtil.setup();

        // lookup

        this.jobManager = getComponentManager().getInstance(JobManager.class);
        this.localExtensionRepository = getComponentManager().getInstance(LocalExtensionRepository.class);
        this.installedExtensionRepository = getComponentManager().getInstance(InstalledExtensionRepository.class);
    }

    protected void beforeRepositoryUtil() throws Exception
    {

    }

    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        registerComponent(ConfigurableDefaultCoreExtensionRepository.class);
    }

    protected Job executeJob(String jobId, Request request, LogLevel failFrom) throws Throwable
    {
        Job installJob = this.jobManager.executeJob(jobId, request);

        List<LogEvent> errors = installJob.getStatus().getLog().getLogsFrom(failFrom);
        if (!errors.isEmpty()) {
            throw errors.get(0).getThrowable() != null ? errors.get(0).getThrowable() : new Exception(errors.get(0)
                .getFormattedMessage());
        }

        return installJob;
    }

    protected InstalledExtension install(ExtensionId extensionId, String namespace) throws Throwable
    {
        return install(extensionId, namespace, LogLevel.WARN);
    }

    protected InstalledExtension install(ExtensionId extensionId, String namespace, LogLevel failFrom) throws Throwable
    {
        install("install", extensionId, namespace, failFrom);

        return this.installedExtensionRepository.resolve(extensionId);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String namespace) throws Throwable
    {
        return installPlan(extensionId, namespace, LogLevel.WARN);
    }

    protected ExtensionPlan installPlan(ExtensionId extensionId, String namespace, LogLevel failFrom) throws Throwable
    {
        Job installJob = install("installplan", extensionId, namespace, failFrom);

        return (ExtensionPlan) installJob.getStatus();
    }

    protected Job install(String jobId, ExtensionId extensionId, String namespace, LogLevel failFrom) throws Throwable
    {
        InstallRequest installRequest = new InstallRequest();
        installRequest.setId(extensionId.getId());
        installRequest.addExtension(extensionId);
        if (namespace != null) {
            installRequest.addNamespace(namespace);
        }

        return executeJob(jobId, installRequest, failFrom);
    }

    protected LocalExtension uninstall(ExtensionId extensionId, String namespace) throws Throwable
    {
        return uninstall(extensionId, namespace, LogLevel.WARN);
    }

    protected LocalExtension uninstall(ExtensionId extensionId, String namespace, LogLevel failFrom) throws Throwable
    {
        uninstall("uninstall", extensionId, namespace, failFrom);

        return this.localExtensionRepository.resolve(extensionId);
    }

    protected DefaultExtensionPlan<UninstallRequest> uninstallPlan(ExtensionId extensionId, String namespace,
        LogLevel failFrom) throws Throwable
    {
        Job uninstallJob = uninstall("installplan", extensionId, namespace, failFrom);

        return (DefaultExtensionPlan<UninstallRequest>) uninstallJob.getStatus();
    }

    protected Job uninstall(String jobId, ExtensionId extensionId, String namespace, LogLevel failFrom)
        throws Throwable
    {
        UninstallRequest uninstallRequest = new UninstallRequest();
        uninstallRequest.setId(extensionId.getId());
        uninstallRequest.addExtension(extensionId);
        if (namespace != null) {
            uninstallRequest.addNamespace(namespace);
        }

        return executeJob(jobId, uninstallRequest, failFrom);
    }

    protected ExtensionPlan upgradePlan() throws Throwable
    {
        return upgradePlan(LogLevel.WARN);
    }

    protected ExtensionPlan upgradePlan(LogLevel failFrom) throws Throwable
    {
        InstallRequest installRequest = new InstallRequest();

        return (ExtensionPlan) executeJob("upgradeplan", installRequest, failFrom).getStatus();
    }
}
