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
package org.xwiki.extension.job.history.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.ExtensionJobHistorySerializer;

import com.thoughtworks.xstream.XStream;

/**
 * Default implementation of {@link ExtensionJobHistorySerializer}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Component
@Singleton
public class DefaultExtensionJobHistorySerializer implements ExtensionJobHistorySerializer
{
    /**
     * Used to serialize and deserialize extension job history records.
     */
    @Inject
    private XStream xstream;

    @Override
    public String serialize(ExtensionJobHistoryRecord record)
    {
        return this.xstream.toXML(record);
    }

    @Override
    public void write(ExtensionJobHistoryRecord record, Writer writer)
    {
        this.xstream.toXML(record, writer);
    }

    @Override
    public void append(ExtensionJobHistoryRecord record, File historyFile) throws IOException
    {
        historyFile.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, true))) {
            write(record, writer);
        }
    }

    @Override
    public List<ExtensionJobHistoryRecord> deserialize(String serializedRecords)
    {
        return this.read(new StringReader(serializedRecords));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ExtensionJobHistoryRecord> read(Reader reader)
    {
        Reader listReader = new CompositeReader(new StringReader("<list>"), reader, new StringReader("</list>"));
        return (List<ExtensionJobHistoryRecord>) this.xstream.fromXML(listReader);
    }

    @Override
    public List<ExtensionJobHistoryRecord> read(File historyFile) throws IOException
    {
        try (FileReader reader = new FileReader(historyFile)) {
            return read(reader);
        }
    }

    @Override
    public List<ExtensionJobHistoryRecord> clone(List<ExtensionJobHistoryRecord> records)
    {
        return deserialize(this.xstream.toXML(records));
    }
}
