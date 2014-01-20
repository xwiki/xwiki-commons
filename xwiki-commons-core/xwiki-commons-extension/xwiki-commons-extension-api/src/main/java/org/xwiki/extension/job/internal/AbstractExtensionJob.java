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
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.event.ExtensionInstalledEvent;
import org.xwiki.extension.event.ExtensionUninstalledEvent;
import org.xwiki.extension.event.ExtensionUpgradedEvent;
import org.xwiki.extension.handler.ExtensionHandlerManager;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.job.internal.AbstractJob;
import org.xwiki.job.internal.AbstractJobStatus;
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
public abstract class AbstractExtensionJob<R extends ExtensionRequest, S extends AbstractJobStatus<R>> extends
    AbstractJob<R, S>
{
    /**
     * The key to use to access the context extension plan.
     */
    public static final String CONTEXTKEY_PLAN = "job.extension.plan";

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
        notifyPushLevelProgress(actions.size());

        try {
            for (ExtensionPlanAction action : actions) {
                if (action.getAction() != Action.NONE) {
                    applyAction(action);
                }

                notifyStepPropress();
            }
        } finally {
            notifyPopLevelProgress();
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
                "Applying [{}] for extension [{}] on namespace [{}] from previous extension [{}]", action.getAction(),
                extension.getId(), namespace, previousExtensionsIds);
        } else {
            previousExtensionsIds = null;
        }

        notifyPushLevelProgress(2);

        try {
            if (action.getAction() == Action.UNINSTALL) {
                InstalledExtension installedExtension = (InstalledExtension) action.getExtension();

                notifyStepPropress();

                // Uninstall
                uninstallExtension(installedExtension, namespace);
            } else {
                // Store extension in local repository
                LocalExtension localExtension = this.localExtensionRepository.resolve(extension.getId());

                notifyStepPropress();

                // Install
                installExtension(localExtension, previousExtensions, namespace, action.isDependency());
            }

            if (getRequest().isVerbose()) {
                this.logger.info(getTranslationMarker(action, "success", false),
                    "Successfully applied [{}] for extension [{}] on namespace [{}] from previous extension [{}]",
                    action.getAction(), extension.getId(), namespace, previousExtensionsIds);
            }
        } catch (ExtensionException e) {
            if (getRequest().isVerbose()) {
                this.logger.error(getTranslationMarker(action, "failure", false),
                    "Failed to apply [{}] for extension [{}] on namespace [{}] from previous extension [{}]",
                    action.getAction(), extension.getId(), namespace, previousExtensionsIds);
            }

            throw e;
        } finally {
            notifyPopLevelProgress();
        }
    }

    /**
     * @param installedExtension the existing extension
     * @param namespace the namespace in which to perform the action
     * @throws UninstallException failed to uninstall extension
     */
    private void uninstallExtension(InstalledExtension installedExtension, String namespace) throws UninstallException
    {
        // Unload extension
        this.extensionHandlerManager.uninstall(installedExtension, namespace, getRequest());

        // Uninstall from local repository
        this.installedExtensionRepository.uninstallExtension(installedExtension, namespace);

        this.observationManager.notify(new ExtensionUninstalledEvent(installedExtension.getId(), namespace),
            installedExtension);
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
        if (previousExtensions.isEmpty()) {
            this.extensionHandlerManager.install(extension, namespace, getRequest());

            InstalledExtension installedExtension =
                this.installedExtensionRepository.installExtension(extension, namespace, dependency);

            this.observationManager.notify(new ExtensionInstalledEvent(extension.getId(), namespace),
                installedExtension);
        } else {
            this.extensionHandlerManager.upgrade(previousExtensions, extension, namespace, getRequest());

            for (InstalledExtension previousExtension : previousExtensions) {
                try {
                    this.installedExtensionRepository.uninstallExtension(previousExtension, namespace);
                } catch (UninstallException e) {
                    this.logger.error("Failed to uninstall extension [" + previousExtension.getId() + "]", e);
                }
            }

            InstalledExtension installedExtension =
                this.installedExtensionRepository.installExtension(extension, namespace, dependency);

            this.observationManager.notify(new ExtensionUpgradedEvent(extension.getId(), namespace),
                installedExtension, previousExtensions);
        }
    }
}
