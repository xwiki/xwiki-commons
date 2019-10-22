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
package org.xwiki.logging.internal.tail;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.Logger;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.internal.ListLogTailResult;
import org.xwiki.logging.tail.EmptyLogTailResult;
import org.xwiki.logging.tail.LogTailResult;

/**
 * Read and write the log in XStream XML format.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public abstract class AbstractFileLoggerTail extends AbstractLoggerTail implements Disposable
{
    protected static final String FAILED_STORE_LOG = "Failed to store the log";

    protected static final String FILE_EXTENSION = ".log";

    @Inject
    protected org.slf4j.Logger componentLogger;

    protected final List<IndexEntry> index = new CopyOnWriteArrayList<>();

    protected File logFile;

    protected RandomAccessFile logStore;

    protected long logStoreLength = -1;

    protected File indexFile;

    protected Writer indexStore;

    protected class IndexEntry
    {
        private final LogLevel level;

        private Long position;

        public IndexEntry(Long position, LogLevel level)
        {
            this.level = level;
            this.position = position;
        }

        @Override
        public String toString()
        {
            return this.position + ":" + this.level;
        }
    }

    protected class FileLoggerTailIterator implements Iterator<LogEvent>
    {
        private int current;

        @Override
        public boolean hasNext()
        {
            return this.current < index.size();
        }

        @Override
        public LogEvent next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            try {
                return getLogEvent(this.current++);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get log event in [" + logFile + "]", e);
            }
        }
    }

    /**
     * @param path the base path of the log
     * @return true of a log has been stored at this location
     */
    public static boolean exist(Path path)
    {
        return exist(path, FILE_EXTENSION);
    }

    /**
     * @param path the base path of the log
     * @param extension the extension of the file containing the log
     * @return true of a log has been stored at this location
     */
    public static boolean exist(Path path, String extension)
    {
        return getLogFile(path, extension).exists();
    }

    protected static File getLogFile(Path path, String extension)
    {
        File logFile = path.toFile();
        if (!logFile.getName().endsWith(extension)) {
            logFile = new File(logFile.getParentFile(), logFile.getName() + extension);
        }

        return logFile;
    }

    /**
     * @param path the base path of the log
     * @param readonly true of the log is readonly
     * @throws IOException when failing to create the log files
     */
    public void initialize(Path path, boolean readonly) throws IOException
    {
        synchronized (this) {
            // The log file
            this.logFile = path.toFile();
            String extension = getFileExtension();
            if (!this.logFile.getName().endsWith(extension)) {
                this.logFile = new File(this.logFile.getParentFile(), this.logFile.getName() + extension);
            }
            this.logFile.getParentFile().mkdirs();

            // The index file
            this.indexFile = new File(this.logFile.getParentFile(),
                this.logFile.getName().substring(0, this.logFile.getName().length() - extension.length()) + ".index");

            if (!readonly) {
                // Overwrite the current one if it exist
                Files.deleteIfExists(this.logFile.toPath());

                // Open the log store
                this.logStore = new RandomAccessFile(this.logFile, "rw");

                this.indexStore = new FileWriter(this.indexFile, false);
            } else if (indexFile.exists()) {
                // Load the existing index
                loadIndex();
            }

            // Remember the date of log file to catch external modifications
            this.logStoreLength = this.logFile.length();
        }
    }

    protected boolean isReadOnly()
    {
        return this.indexStore == null;
    }

    protected boolean open() throws FileNotFoundException
    {
        if (this.logStore == null) {
            this.logStore = new RandomAccessFile(this.logFile, "r");

            return true;
        }

        return false;
    }

    protected void close(boolean open) throws IOException
    {
        if (open) {
            synchronized (this) {
                // Make sure to write everything
                flush();

                if (this.indexStore != null) {
                    this.indexStore.close();
                    this.indexStore = null;
                }

                // Close the store
                if (this.logStore != null) {
                    this.logStore.close();
                    this.logStore = null;
                }
            }
        }
    }

    /**
     * @return the extension of the file containing the log
     */
    protected String getFileExtension()
    {
        return FILE_EXTENSION;
    }

    private void loadIndex()
    {
        this.index.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.indexFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int i = line.indexOf(':');

                this.index
                    .add(new IndexEntry(Long.valueOf(line.substring(0, i)), LogLevel.valueOf(line.substring(i + 1))));
            }
        } catch (Exception e) {
            this.componentLogger.warn("Failed to read log index file [{}]: {}", indexFile,
                ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Override
    public void log(LogEvent logEvent)
    {
        if (this.logStore != null) {
            writeLog(logEvent);
        }
    }

    private void writeLog(LogEvent logEvent)
    {
        // We can't store this log since it has a good chance of creating a infinine loop
        // We don't use #equals for performance reason, it works because it's the exact same String instance
        if (FAILED_STORE_LOG != logEvent.getMessage()) {
            synchronized (this) {
                try {
                    IndexEntry indexEntry = new IndexEntry(this.logStore.length(), logEvent.getLevel());
                    this.index.add(indexEntry);

                    // Go to the end of the file
                    this.logStore.seek(indexEntry.position);

                    write(logEvent, this.logStore);

                    // Add a new line for readability
                    this.logStore.write('\n');

                    this.logStoreLength = this.logFile.length();

                    writeIndex(indexEntry.position, logEvent.getLevel());
                } catch (Exception e) {
                    this.componentLogger.error(Logger.ROOT_MARKER, FAILED_STORE_LOG, e);
                }
            }
        }
    }

    private void writeIndex(long position, LogLevel level) throws IOException
    {
        this.indexStore.append(String.valueOf(position));
        this.indexStore.append(':');
        this.indexStore.append(level.toString());
        this.indexStore.append('\n');
    }

    @Override
    public LogEvent getLogEvent(int index)
    {
        checkChanged();

        IndexEntry indexEntry = getIndexEntry(index);
        if (indexEntry == null) {
            // Not entry associated with this index
            return null;
        }

        return getLogEvent(index, indexEntry);
    }

    private LogEvent getLogEvent(int index, IndexEntry indexEntry)
    {
        try {
            synchronized (this) {
                boolean open = open();

                try {
                    this.logStore.seek(indexEntry.position);

                    Long toPosition =
                        this.index.size() > index + 1 ? this.index.get(index + 1).position : this.logStore.length();

                    BoundedInputStream stream = new BoundedInputStream(new InputStreamDataInput(this.logStore),
                        toPosition - indexEntry.position);

                    return read(stream);
                } finally {
                    close(open);
                }
            }
        } catch (Exception e) {
            this.componentLogger.error("Faile to retrieve log for [{}]", this.logFile, e);

            return null;
        }
    }

    private void checkChanged()
    {
        // Make sure the log still exist and is not empty
        if (isReadOnly() && !this.index.isEmpty()
            && (!this.logFile.exists() || this.logFile.length() != this.logStoreLength)) {
            // Reset the this log index if something else started modifying the log file
            this.index.clear();
        }
    }

    @Override
    public LogEvent getFirstLogEvent(LogLevel from)
    {
        checkChanged();

        for (int i = 0; i < this.index.size(); ++i) {
            IndexEntry indexEntry = getIndexEntry(i);
            if (indexEntry == null) {
                // Generally means the log has been modify by another process during the for loop
                break;
            }

            if (isLogLevel(indexEntry, from)) {
                return getLogEvent(i, indexEntry);
            }
        }

        return null;
    }

    private IndexEntry getIndexEntry(int i)
    {
        if (this.index.size() <= i) {
            return null;
        }

        IndexEntry indexEntry = this.index.get(i);

        if (this.logFile.length() < indexEntry.position) {
            synchronized (this) {
                if (this.logFile.length() == 0) {
                    // Looks like the log file was deleted/emptied by another thread
                    // Reset the index
                    this.index.clear();
                } else {
                    // Looks like the index has been modified by another thread
                    // Try to reload the index
                    loadIndex();
                }

                indexEntry = i < this.index.size() ? this.index.get(i) : null;
            }
        }

        return indexEntry;
    }

    private boolean isLogLevel(IndexEntry indexEntry, LogLevel from)
    {
        return from == null || indexEntry.level.compareTo(from) <= 0;
    }

    @Override
    public LogEvent getLastLogEvent(LogLevel from)
    {
        checkChanged();

        IndexEntry lastIndexEntry = null;
        int lastIndex = -1;

        for (int i = 0; i < this.index.size(); ++i) {
            IndexEntry indexEntry = getIndexEntry(i);
            if (indexEntry == null) {
                // Generally means the log has been modify by another process during the for loop
                break;
            }

            if (isLogLevel(indexEntry, from)) {
                lastIndexEntry = indexEntry;
                lastIndex = i;
            }
        }

        return lastIndexEntry != null ? getLogEvent(lastIndex, lastIndexEntry) : null;
    }

    @Override
    public LogTailResult getLogEvents(LogLevel from, int offset, int limit)
    {
        synchronized (this) {
            if (this.index.size() <= offset) {
                return EmptyLogTailResult.INSTANCE;
            }

            int fromIndex = offset;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            int toIndex = fromIndex + limit;
            if (toIndex <= fromIndex || toIndex > this.index.size()) {
                toIndex = this.index.size();
            }

            List<LogEvent> events = new ArrayList<>(toIndex - fromIndex);

            try {
                getLogEvents(toIndex, fromIndex, from, events);
            } catch (Exception e) {
                this.componentLogger.error("Faile to retrieve log for [{}]", this.logFile, e);
            }

            return new ListLogTailResult(events);
        }
    }

    private void getLogEvents(int toIndex, int fromIndex, LogLevel from, List<LogEvent> events) throws IOException
    {
        boolean open = open();

        try {
            for (int i = fromIndex; i < toIndex; ++i) {
                IndexEntry indexEntry = this.index.get(i);

                if (isLogLevel(indexEntry, from)) {
                    events.add(getLogEvent(i));
                }
            }
        } finally {
            close(open);
        }
    }

    @Override
    public boolean hasLogLevel(LogLevel from)
    {
        for (IndexEntry indexEntry : this.index) {
            if (isLogLevel(indexEntry, from)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int size()
    {
        return this.index.size();
    }

    @Override
    public Iterator<LogEvent> iterator()
    {
        return new FileLoggerTailIterator();
    }

    @Override
    public void flush() throws IOException
    {
        if (this.indexStore != null) {
            this.indexStore.flush();
        }
    }

    @Override
    public void close() throws IOException
    {
        close(this.logStore != null);
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        try {
            close();
        } catch (Exception e) {
            throw new ComponentLifecycleException("Failed to close the logger", e);
        }

        this.index.clear();
    }

    // Abstracts

    protected abstract LogEvent read(InputStream input);

    protected abstract void write(LogEvent logEvent, DataOutput output);
}
