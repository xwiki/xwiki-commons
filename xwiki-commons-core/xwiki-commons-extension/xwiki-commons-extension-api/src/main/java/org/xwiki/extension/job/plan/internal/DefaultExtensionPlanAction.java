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
package org.xwiki.extension.job.plan.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionRewriter;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.job.plan.ExtensionPlanAction;

/**
 * An action to perform as part of an extension plan.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultExtensionPlanAction implements ExtensionPlanAction
{
    /**
     * @see #getExtension()
     */
    private Extension extension;

    /**
     * @see #getRewrittenExtension()
     */
    private Extension rewrittenExtension;

    /**
     * @see #getPreviousExtension()
     */
    private Collection<InstalledExtension> previousExtensions;

    /**
     * @see Action
     */
    private Action action;

    /**
     * @see #getNamespace()
     */
    private String namespace;

    /**
     * @see #isDependency()
     */
    private boolean dependency;

    /**
     * @param extension the extension on which to perform the action
     * @param rewrittenExtension the result of {@link ExtensionRewriter#rewrite(Extension)} on the extension on which to
     *            perform the actionO
     * @param previousExtensions the currently installed extensions. Used when upgrading
     * @param action the action to perform
     * @param namespace the namespace in which the action should be executed
     * @param dependency indicate indicate if the extension is a dependency of another one only in the plan
     */
    public DefaultExtensionPlanAction(Extension extension, Extension rewrittenExtension,
        Collection<InstalledExtension> previousExtensions, Action action, String namespace, boolean dependency)
    {
        this.extension = extension;
        this.rewrittenExtension = rewrittenExtension;

        this.previousExtensions = previousExtensions != null ? new LinkedHashSet<InstalledExtension>(previousExtensions)
            : Collections.<InstalledExtension>emptyList();
        this.action = action;
        this.namespace = namespace;
        this.dependency = dependency;
    }

    @Override
    public Extension getExtension()
    {
        return this.extension;
    }

    @Override
    public Extension getRewrittenExtension()
    {
        return this.rewrittenExtension;
    }

    @Override
    @Deprecated
    public InstalledExtension getPreviousExtension()
    {
        return this.previousExtensions.isEmpty() ? null : this.previousExtensions.iterator().next();
    }

    @Override
    public Collection<InstalledExtension> getPreviousExtensions()
    {
        return this.previousExtensions;
    }

    @Override
    public Action getAction()
    {
        return this.action;
    }

    @Override
    public String getNamespace()
    {
        return this.namespace;
    }

    @Override
    public boolean isDependency()
    {
        return this.dependency;
    }

    // Object

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.extension);
        builder.append(this.namespace);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        boolean equals;

        if (obj instanceof ExtensionPlanAction) {
            ExtensionPlanAction epa = (ExtensionPlanAction) obj;
            equals = this.extension.equals(epa.getExtension()) && Objects.equals(this.namespace, epa.getNamespace());
        } else {
            equals = false;
        }

        return equals;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(this.action);

        builder.append(": ");

        builder.append(this.extension);

        builder.append(" (");
        builder.append(this.namespace);
        if (!this.previousExtensions.isEmpty()) {
            builder.append(", ");
            builder.append(this.previousExtensions);
        }
        builder.append(')');

        return builder.toString();
    }
}
