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
package org.xwiki.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.directive.ForeachScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the standard {@link VelocityContext} to add some retro compatibility (for example support for $velocityCount
 * and $velocityHasNext).
 * 
 * @version $Id$
 * @since 12.0RC1
 */
public class XWikiVelocityContext extends VelocityContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XWikiVelocityContext.class);

    private static final String VELOCITYCOUNT = "velocityCount";

    private static final String VELOCITYHASNEXT = "velocityHasNext";

    private final boolean logDeprecated;

    /**
     * Creates a new instance (with no inner context).
     */
    public XWikiVelocityContext()
    {
        this.logDeprecated = true;
    }

    /**
     * Chaining constructor, used when you want to wrap a context in another. The inner context will be 'read only' -
     * put() calls to the wrapping context will only effect the outermost context
     *
     * @param innerContext The <code>Context</code> implementation to wrap.
     */
    public XWikiVelocityContext(Context innerContext)
    {
        this(innerContext, true);
    }

    /**
     * Chaining constructor, used when you want to wrap a context in another. The inner context will be 'read only' -
     * put() calls to the wrapping context will only effect the outermost context
     *
     * @param innerContext The <code>Context</code> implementation to wrap.
     * @param logDeprecated true if use of deprecated binding should be logged
     * @since 12.4
     * @since 11.10.6
     */
    public XWikiVelocityContext(Context innerContext, boolean logDeprecated)
    {
        super(innerContext);

        this.logDeprecated = logDeprecated;
    }

    private ForeachScope getForeachScope()
    {
        return (ForeachScope) get("foreach");
    }

    private Integer getVelocityCount()
    {
        ForeachScope foreachScope = getForeachScope();

        if (foreachScope != null) {
            warnDeprecatedBinding(VELOCITYCOUNT, foreachScope);
        }

        return foreachScope != null ? foreachScope.getCount() : null;
    }

    private Boolean getVelocityHasNext()
    {
        ForeachScope foreachScope = getForeachScope();

        if (foreachScope != null) {
            warnDeprecatedBinding(VELOCITYHASNEXT, foreachScope);
        }

        return foreachScope != null ? foreachScope.hasNext() : null;
    }

    private void warnDeprecatedBinding(String binding, ForeachScope foreachScope)
    {
        if (this.logDeprecated) {
            LOGGER.warn("Deprecated binding [${}] used in [{}]", binding, foreachScope.getInfo().getTemplate());
        }
    }

    @Override
    public Object get(String key)
    {
        Object value;

        if (!containsKey(key)) {
            // Retro compatibility
            switch (key) {
                // Replaced by $foreach.count
                case VELOCITYCOUNT:
                    value = getVelocityCount();
                    if (value != null) {
                        return value;
                    }

                    break;

                // Replaced by $foreach.hasNext
                case VELOCITYHASNEXT:
                    value = getVelocityHasNext();
                    if (value != null) {
                        return value;
                    }

                    break;

                default:
                    break;
            }
        }

        return super.get(key);
    }
}
