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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * The interface used to serialize and deserialize the {@link ExtensionJobHistory}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Role
@Unstable
public interface ExtensionJobHistorySerializer
{
    /**
     * Serializes a given history record.
     * 
     * @param record the history record to serialize
     * @return the string serialization of the given history record
     */
    String serialize(ExtensionJobHistoryRecord record);

    /**
     * Serializes a given history record and passes the result to the given writer.
     * 
     * @param record the history record to serialize
     * @param writer where to write the serialized history record
     */
    void write(ExtensionJobHistoryRecord record, Writer writer);

    /**
     * Appends the serialization of a given history record to a specified partial history file.
     * 
     * @param record the history record to serialize
     * @param historyFile the history file where to append the result
     * @throws IOException if it fails to append the serialized history record to the specified file
     */
    void append(ExtensionJobHistoryRecord record, File historyFile) throws IOException;

    /**
     * Deserializes a list of history records.
     * 
     * @param serializedRecords the string containing the serialized history records
     * @return the list of history records that have been deserialized
     */
    List<ExtensionJobHistoryRecord> deserialize(String serializedRecords);

    /**
     * Deserializes a list of history records from a given reader.
     * 
     * @param reader from where to read the history records
     * @return the list of history records that have been deserialized
     */
    List<ExtensionJobHistoryRecord> read(Reader reader);

    /**
     * Deserializes all the history records that have been serialized in the given file.
     * 
     * @param historyFile the history file containing the serialized history records
     * @return the list of history records that have been deserialized
     * @throws IOException if it fails to read from the specified file
     */
    List<ExtensionJobHistoryRecord> read(File historyFile) throws IOException;

    /**
     * Clones a list of history records by serializing and deserializing it.
     * 
     * @param records the list of history records to clone
     * @return the cloned list
     */
    List<ExtensionJobHistoryRecord> clone(List<ExtensionJobHistoryRecord> records);
}
