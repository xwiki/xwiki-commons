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
package org.xwiki.extension.job.plan;

import java.util.Collection;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionRewriter;
import org.xwiki.extension.InstalledExtension;

/**
 * An action to perform as part of an extension plan.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface ExtensionPlanAction
{
    /**
     * The action to execute.
     *
     * @version $Id$
     */
    enum Action
    {
        /**
         * Nothing to do. Just here for information as why nothing is done here.
         */
        NONE,

        /**
         * Install the extension.
         */
        INSTALL,

        /**
         * Upgrade the extension.
         */
        UPGRADE,

        /**
         * Downgrade the extension.
         */
        DOWNGRADE,

        /**
         * Uninstall the extension.
         */
        UNINSTALL,

        /**
         * Repair the extension.
         * <p>
         * Mostly mean mark the extension as valid.
         * 
         * @since 8.1M1
         */
        REPAIR
    }

    /**
     * @return the extension on which to perform the action
     */
    Extension getExtension();

    /**
     * @return the result of {@link ExtensionRewriter#rewrite(Extension)} on the extension on which to perform the
     *         action
     * @since 8.4.2
     * @since 9.0RC1
     */
    default Extension getRewrittenExtension()
    {
        return getExtension();
    }

    /**
     * @return the currently installed extension. Used when upgrading.
     * @deprecated since 5.0RC1 used {@link #getPreviousExtensions()} instead
     */
    @Deprecated
    InstalledExtension getPreviousExtension();

    /**
     * @return the currently installed extensions. Used when upgrading.
     */
    Collection<InstalledExtension> getPreviousExtensions();

    /**
     * @return the action to perform
     */
    Action getAction();

    /**
     * @return the namespace in which the action should be executed
     */
    String getNamespace();

    /**
     * @return indicate indicate if the extension is a dependency of another one only in the plan
     */
    boolean isDependency();

}
