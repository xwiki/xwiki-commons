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
package org.xwiki.extension.job.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.event.ExtensionInstallFailedEvent;
import org.xwiki.extension.event.ExtensionInstalledEvent;
import org.xwiki.extension.event.ExtensionInstallingEvent;
import org.xwiki.extension.event.ExtensionUninstallFailedEvent;
import org.xwiki.extension.event.ExtensionUninstalledEvent;
import org.xwiki.extension.event.ExtensionUninstallingEvent;
import org.xwiki.extension.event.ExtensionUpgradeFailedEvent;
import org.xwiki.extension.event.ExtensionUpgradedEvent;
import org.xwiki.extension.event.ExtensionUpgradingEvent;
import org.xwiki.extension.handler.ExtensionHandlerManager;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.Request;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.marker.BeginTranslationMarker;
import org.xwiki.logging.marker.EndTranslationMarker;
import org.xwiki.logging.marker.TranslationMarker;

/**
 * Base class for any Job dealing with extensions.
 *
 * @param <R> the type of the request
 * @param <S> the type of the {@link org.xwiki.job.event.status.JobStatus}
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractExtensionJob<R extends ExtensionRequest, S extends JobStatus> extends AbstractJob<R, S>
    implements GroupedJob
{
    /**
     * The key to use to access the context extension plan.
     */
    public static final String CONTEXTKEY_PLAN = "job.extension.plan";

    /**
     * The root group of all extension related jobs.
     */
    public static final JobGroupPath ROOT_GROUP = new JobGroupPath(Arrays.asList("extension"));

    /**
     * Used to manipulate local extension repository.
     */
    @Inject
    protected LocalExtensionRepository localExtensionRepository;

    /**
     * Used to install the extension itself depending of its type.
     */
    @Inject
    protected ExtensionHandlerManager extensionHandlerManager;

    /**
     * Used to manipulate installed extension repository.
     */
    @Inject
    protected InstalledExtensionRepository installedExtensionRepository;

    protected JobGroupPath groupPath;

    @Override
    public void initialize(Request request)
    {
        super.initialize(request);

        // Build the group path
        if (getRequest().getNamespaces() != null && getRequest().getNamespaces().size() == 1) {
            this.groupPath = new JobGroupPath(getRequest().getNamespaces().iterator().next(), ROOT_GROUP);
        } else {
            this.groupPath = ROOT_GROUP;
        }
    }

    @Override
    public JobGroupPath getGroupPath()
    {
        return this.groupPath;
    }

    private static TranslationMarker getTranslationMarker(ExtensionPlanAction action, String extension, boolean begin)
    {
        StringBuilder str = new StringBuilder("extension.log.job.");

        str.append(action.getAction().toString().toLowerCase());

        if (extension != null) {
            str.append('.');
            str.append(extension);
        }

        str.append('.');
        str.append(begin ? "begin" : "end");

        if (action.getNamespace() != null) {
            str.append("OnNamespace");
        }

        return begin ? new BeginTranslationMarker(str.toString()) : new EndTranslationMarker(str.toString());
    }

    /**
     * @param actions the actions to apply
     * @throws ExtensionException failed to apply action
     */
    protected void applyActions(Collection<ExtensionPlanAction> actions) throws ExtensionException
    {
        this.progressManager.pushLevelProgress(actions.size(), this);

        try {
            for (ExtensionPlanAction action : actions) {
                this.progressManager.startStep(this);

                if (action.getAction() != Action.NONE) {
                    applyAction(action);
                }

                this.progressManager.endStep(this);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    /**
     * @param action the action to perform
     * @throws ExtensionException failed to apply action
     */
    protected void applyAction(ExtensionPlanAction action) throws ExtensionException
    {
        Collection<InstalledExtension> previousExtensions = action.getPreviousExtensions();

        Extension extension = action.getExtension();
        String namespace = action.getNamespace();

        List<ExtensionId> previousExtensionsIds;

        if (getRequest().isVerbose()) {
            previousExtensionsIds = new ArrayList<ExtensionId>(previousExtensions.size());
            for (InstalledExtension previousExtension : previousExtensions) {
                previousExtensionsIds.add(previousExtension.getId());
            }

            this.logger.info(getTranslationMarker(action, null, true),
                "Applying [{}] for extension [{}] on namespace [{}] from previous extension(s) [{}]",
                action.getAction(), extension.getId(), namespace, previousExtensionsIds);
        } else {
            previousExtensionsIds = null;
        }

        try {
            if (action.getAction() == Action.REPAIR) {
                InstalledExtension installedExtension = (InstalledExtension) action.getExtension();

                // Initialize
                repairExtension(installedExtension, namespace);
            } else if (action.getAction() == Action.UNINSTALL) {
                InstalledExtension installedExtension = (InstalledExtension) action.getExtension();

                // Uninstall
                uninstallExtension(installedExtension, namespace);
            } else {
                // Store extension in local repository
                LocalExtension localExtension = this.localExtensionRepository.resolve(extension.getId());

                // Install
                installExtension(localExtension, previousExtensions, namespace, action.isDependency());
            }

            if (getRequest().isVerbose()) {
                this.logger.info(getTranslationMarker(action, "success", false),
                    "Successfully applied [{}] for extension [{}] on namespace [{}] from previous extension(s) [{}]",
                    action.getAction(), extension.getId(), namespace, previousExtensionsIds);
            }
        } catch (ExtensionException e) {
            if (getRequest().isVerbose()) {
                this.logger.error(getTranslationMarker(action, "failure", false),
                    "Failed to apply [{}] for extension [{}] on namespace [{}] from previous extension(s) [{}]",
                    action.getAction(), extension.getId(), namespace, previousExtensionsIds, e);
            }

            throw e;
        }
    }

    /**
     * @param installedExtension the existing extension
     * @param namespace the namespace in which to perform the action
     * @throws ExtensionException failed to initialize extension
     */
    private void repairExtension(InstalledExtension installedExtension, String namespace) throws ExtensionException
    {
        this.observationManager.notify(new ExtensionInstallingEvent(installedExtension.getId(), namespace),
            installedExtension);

        boolean success = false;
        try {
            // Initialize extension (invalid extensions are not initialized at startup)
            this.extensionHandlerManager.initialize(installedExtension, namespace);

            // Repair the extension
            this.installedExtensionRepository.installExtension(installedExtension, namespace,
                installedExtension.isDependency(namespace));

            success = true;
        } finally {
            if (success) {
                this.observationManager.notify(new ExtensionInstalledEvent(installedExtension.getId(), namespace),
                    installedExtension);
            } else {
                this.observationManager.notify(new ExtensionInstallFailedEvent(installedExtension.getId(), namespace),
                    installedExtension);
            }
        }
    }

    /**
     * @param installedExtension the existing extension
     * @param namespace the namespace in which to perform the action
     * @throws UninstallException failed to uninstall extension
     */
    private void uninstallExtension(InstalledExtension installedExtension, String namespace) throws UninstallException
    {
        this.observationManager.notify(new ExtensionUninstallingEvent(installedExtension.getId(), namespace),
            installedExtension);

        boolean success = false;
        try {
            // Unload extension
            this.extensionHandlerManager.uninstall(installedExtension, namespace, getRequest());

            // Uninstall from local repository
            this.installedExtensionRepository.uninstallExtension(installedExtension, namespace);

            success = true;
        } finally {
            if (success) {
                this.observationManager.notify(new ExtensionUninstalledEvent(installedExtension.getId(), namespace),
                    installedExtension);
            } else {
                this.observationManager.notify(new ExtensionUninstallFailedEvent(installedExtension.getId(), namespace),
                    installedExtension);
            }
        }
    }

    /**
     * @param extension the extension
     * @param previousExtensions the previous extensions when upgrading
     * @param namespace the namespace in which to perform the action
     * @param dependency indicate if the extension has been installed as dependency
     * @throws InstallException failed to install extension
     */
    private void installExtension(LocalExtension extension, Collection<InstalledExtension> previousExtensions,
        String namespace, boolean dependency) throws InstallException
    {
        Map<String, Object> properties = getRequest().getProperty(InstallRequest.PROPERTY_EXTENSION_PROPERTIES,
            Collections.<String, Object>emptyMap());
        if (previousExtensions.isEmpty()) {
            this.observationManager.notify(new ExtensionInstallingEvent(extension.getId(), namespace), extension);

            InstalledExtension installedExtension = null;
            try {
                this.extensionHandlerManager.install(extension, namespace, getRequest());

                installedExtension =
                    this.installedExtensionRepository.installExtension(extension, namespace, dependency, properties);
            } finally {
                if (installedExtension != null) {
                    this.observationManager.notify(new ExtensionInstalledEvent(extension.getId(), namespace),
                        installedExtension);
                } else {
                    this.observationManager.notify(new ExtensionInstallFailedEvent(extension.getId(), namespace),
                        extension);
                }
            }
        } else {
            this.observationManager.notify(new ExtensionUpgradingEvent(extension.getId(), namespace), extension,
                previousExtensions);

            InstalledExtension installedExtension = null;
            try {
                this.extensionHandlerManager.upgrade(previousExtensions, extension, namespace, getRequest());

                for (InstalledExtension previousExtension : previousExtensions) {
                    try {
                        this.installedExtensionRepository.uninstallExtension(previousExtension, namespace);
                    } catch (UninstallException e) {
                        this.logger.error("Failed to uninstall extension [{}]", previousExtension.getId(), e);
                    }
                }

                installedExtension =
                    this.installedExtensionRepository.installExtension(extension, namespace, dependency, properties);
            } finally {
                if (installedExtension != null) {
                    this.observationManager.notify(new ExtensionUpgradedEvent(extension.getId(), namespace),
                        installedExtension, previousExtensions);
                } else {
                    this.observationManager.notify(new ExtensionUpgradeFailedEvent(extension.getId(), namespace),
                        extension, previousExtensions);
                }
            }
        }
    }
}
