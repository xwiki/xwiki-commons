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
package org.xwiki.extension.job.history;

import java.io.Serializable;

import org.xwiki.component.annotation.Role;

/**
 * Component used to record the answers given to a specific type of questions and to replay the recorded answers later
 * in case those questions are asked again.
 * 
 * @param <T> the type of question handled by this recorder
 * @version $Id$
 * @since 7.1RC1
 */
@Role
public interface QuestionRecorder<T> extends Serializable
{
    /**
     * Record the answer to the given question. The answer is included in the object that represents the question.
     * 
     * @param question the question that has been asked and its answer
     */
    void record(T question);

    /**
     * Respond to the given question using a recorded answer, if available.
     * 
     * @param question the question that must be answered
     * @return {@code true} if there is a recorded answer for the given question (which is used to answer the question),
     *         {@code false} if there is not recorded answer for the given question
     */
    boolean replay(T question);
}
