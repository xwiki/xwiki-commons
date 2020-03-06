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
package org.xwiki.tool.spoon;

import java.util.ArrayList;
import java.util.List;

import spoon.SpoonException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/**
 * Common code for writing XWiki Spoon Processors. Handles errors and reporting at the end.
 *
 * @param <E> the Spoon element type
 * @version $Id$
 */
public abstract class AbstractXWikiProcessor<E extends CtElement> extends AbstractProcessor<E>
{
    private List<String> errors = new ArrayList<>();

    /**
     * Registers an error.
     *
     * @param error the text of the error to register so that it's reported at the end of the module processing
     */
    protected void registerError(String error)
    {
        this.errors.add(error);
    }

    @Override
    public void processingDone()
    {
        if (!this.errors.isEmpty()) {
            StringBuilder builder = new StringBuilder("The following errors were found:\n");
            for (String error : this.errors) {
                builder.append("- ").append(error).append('\n');
            }
            throw new SpoonException(builder.toString());
        }
    }
}
