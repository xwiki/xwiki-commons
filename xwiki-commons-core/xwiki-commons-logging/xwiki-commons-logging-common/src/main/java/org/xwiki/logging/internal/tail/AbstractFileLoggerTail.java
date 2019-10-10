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
import org.slf4j.Logger;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.logging.LogLevel;
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
    @Inject
    protected Logger componentLogger;

    protected final List<IndexEntry> index = new CopyOnWriteArrayList<>();

    protected File logFile;

    protected RandomAccessFile logStore;

    protected Writer indexStore;

    protected boolean closed = true;

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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @param path the base path of the log
     * @param readonly true of the log is readonly
     * @throws IOException when failing to create the log files
     */
    public void initialize(Path path, boolean readonly) throws IOException
    {
        // The log file
        this.logFile = path.toFile();
        String extension = getFileExtension();
        if (!this.logFile.getName().endsWith(extension)) {
            this.logFile = new File(this.logFile.getParentFile(), this.logFile.getName() + extension);
        }
        this.logFile.getParentFile().mkdirs();

        // The index file
        File indexFile = new File(this.logFile.getParentFile(),
            this.logFile.getName().substring(0, this.logFile.getName().length() - extension.length()) + ".index");

        if (!readonly) {
            // Overwrite the current one if it exist
            Files.deleteIfExists(this.logFile.toPath());

            // Open the log store
            open();

            this.indexStore = new FileWriter(indexFile, false);
        } else if (indexFile.exists()) {
            // Load the existing index
            loadIndex(indexFile);
        }
    }

    protected boolean open() throws FileNotFoundException
    {
        if (this.closed) {
            this.logStore = new RandomAccessFile(this.logFile, "rw");

            this.closed = false;

            return true;
        }

        return false;
    }

    protected void close(boolean open) throws IOException
    {
        if (open) {
            // Indicate the logger is closed
            this.closed = true;

            // Make sure to write everything
            flush();

            // Close the store
            if (this.logStore != null) {
                this.logStore.close();
                this.logStore = null;
            }
            if (this.indexStore != null) {
                this.indexStore.close();
                this.indexStore = null;
            }
        }
    }

    /**
     * @return the extension of the file containing the log
     */
    protected String getFileExtension()
    {
        return ".log";
    }

    private void loadIndex(File indexFile)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))) {
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
        if (!this.closed) {
            writeLog(logEvent);
        }
    }

    private void writeLog(LogEvent logEvent)
    {
        synchronized (this) {
            try {
                IndexEntry indexEntry = new IndexEntry(this.logStore.length(), logEvent.getLevel());
                this.index.add(indexEntry);

                // Go to the end of the file
                this.logStore.seek(indexEntry.position);

                write(logEvent, this.logStore);

                // Add a new line for readability
                this.logStore.write('\n');

                writeIndex(indexEntry.position, logEvent.getLevel());
            } catch (Exception e) {
                // We can't log that in the current thread since it has a good chance of create a infinine loop

                this.componentLogger.error("Failed to store the log", e);
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
    public LogEvent getLogEvent(int index) throws IOException
    {
        if (this.index.size() <= index) {
            return null;
        }

        IndexEntry indexEntry = this.index.get(index);

        return getLogEvent(index, indexEntry);
    }

    private LogEvent getLogEvent(int index, IndexEntry indexEntry) throws IOException
    {
        Long fromPosition = indexEntry.position;

        synchronized (this) {
            boolean open = open();

            try {
                this.logStore.seek(fromPosition);

                Long toPosition =
                    this.index.size() > index + 1 ? this.index.get(index + 1).position : this.logStore.length();

                BoundedInputStream stream =
                    new BoundedInputStream(new InputStreamDataInput(this.logStore), toPosition - fromPosition);

                return read(stream);
            } finally {
                close(open);
            }
        }
    }

    @Override
    public LogEvent getFirstLogEvent(LogLevel from) throws IOException
    {
        for (int i = 0; i < this.index.size(); ++i) {
            IndexEntry indexEntry = this.index.get(i);

            if (isLogLevel(indexEntry, from)) {
                return getLogEvent(i, indexEntry);
            }
        }

        return null;
    }

    private boolean isLogLevel(IndexEntry indexEntry, LogLevel from)
    {
        return from == null || indexEntry.level.compareTo(from) <= 0;
    }

    @Override
    public LogEvent getLastLogEvent(LogLevel from) throws IOException
    {
        IndexEntry lastIndexEntry = null;
        int lastIndex = -1;

        for (int i = 0; i < this.index.size(); ++i) {
            IndexEntry indexEntry = this.index.get(i);

            if (isLogLevel(indexEntry, from)) {
                lastIndexEntry = indexEntry;
                lastIndex = i;
            }
        }

        return lastIndexEntry != null ? getLogEvent(lastIndex, lastIndexEntry) : null;
    }

    @Override
    public LogTailResult getLogEvents(LogLevel from, int offset, int limit) throws IOException
    {
        if (this.index.size() <= offset) {
            return EmptyLogTailResult.INSTANCE;
        }

        synchronized (this) {
            int fromIndex = offset;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            int toIndex = fromIndex + limit;
            if (toIndex <= fromIndex) {
                toIndex = this.index.size();
            }

            List<LogEvent> events = new ArrayList<>(toIndex - fromIndex);

            boolean open = open();

            try {
                for (int i = offset; i < toIndex; ++i) {
                    IndexEntry indexEntry = this.index.get(i);

                    if (isLogLevel(indexEntry, from)) {
                        events.add(getLogEvent(i));
                    }
                }
            } finally {
                close(open);
            }

            return new ListLogTailResult(events);
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
    public void close() throws Exception
    {
        close(!this.closed);
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
