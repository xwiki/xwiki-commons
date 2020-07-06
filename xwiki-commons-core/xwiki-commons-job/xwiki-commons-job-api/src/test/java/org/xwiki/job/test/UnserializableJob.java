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
package org.xwiki.job.test;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.Request;

/**
 * A job with an unserializable status.
 * 
 * @version $Id$
 */
@Component
@Named(UnserializableJob.TYPE)
@Singleton
public class UnserializableJob extends AbstractJob<Request, UnserializableJobStatus>
{
    public static final String TYPE = "unserializable";

    @Override
    public String getType()
    {
        return TYPE;
    }

    @Override
    protected UnserializableJobStatus createNewStatus(Request request)
    {
        return new UnserializableJobStatus(TYPE, request, null, this.observationManager, this.loggerManager);
    }

    @Override
    protected void runInternal() throws Exception
    {

    }
}
