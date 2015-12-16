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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.job.history.ExtensionJobHistory;
import org.xwiki.extension.job.history.ExtensionJobHistoryConfiguration;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.ExtensionJobHistorySerializer;

/**
 * Default {@link ExtensionJobHistory} implementation.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Component
@Singleton
public class DefaultExtensionJobHistory implements ExtensionJobHistory, Initializable, Disposable
{
    private class SaveRunnable implements Runnable
    {
        @Override
        public void run()
        {
            logger.debug("Start extension job history saving thread.");

            while (!Thread.interrupted()) {
                ExtensionJobHistoryRecord record;
                try {
                    record = saveQueue.take();
                } catch (InterruptedException e) {
                    logger.warn("Extension job history saving thread has been interrupted. Root cause [{}].",
                        ExceptionUtils.getRootCauseMessage(e));
                    record = SAVE_QUEUE_END;
                }

                if (record == SAVE_QUEUE_END) {
                    break;
                } else {
                    save(record);
                }
            }

            logger.debug("Stop extension job history saving thread.");
        }
    }

    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy.MM.dd.'xml'");

    private static final ExtensionJobHistoryRecord SAVE_QUEUE_END = new ExtensionJobHistoryRecord("SAVE_QUEUE_END",
        null, null, null, null);

    @Inject
    private Logger logger;

    @Inject
    private ExtensionJobHistoryConfiguration config;

    @Inject
    private ExtensionJobHistorySerializer serializer;

    private final Deque<ExtensionJobHistoryRecord> records = new ConcurrentLinkedDeque<>();

    private BlockingQueue<ExtensionJobHistoryRecord> saveQueue = new LinkedBlockingQueue<>();

    @Override
    public void initialize() throws InitializationException
    {
        load();

        Thread saveThread = new Thread(new SaveRunnable());
        saveThread.setName("XWiki's extension job history saving thread");
        saveThread.setDaemon(true);
        saveThread.setPriority(Thread.MIN_PRIORITY);
        saveThread.start();
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        // Stop the history saving thread by sending the stop signal.
        this.saveQueue.offer(SAVE_QUEUE_END);
    }

    @Override
    public void addRecord(ExtensionJobHistoryRecord record)
    {
        this.records.offerFirst(record);
        this.saveQueue.offer(record);
    }

    @Override
    public List<ExtensionJobHistoryRecord> getRecords(Predicate<ExtensionJobHistoryRecord> filter,
        String offsetRecordId, int limit)
    {
        Iterator<ExtensionJobHistoryRecord> iterator = this.records.iterator();
        if (offsetRecordId != null) {
            while (iterator.hasNext() && !offsetRecordId.equals(iterator.next().getId())) {
                // Do nothing.
            }
        }

        List<ExtensionJobHistoryRecord> page = new ArrayList<>();
        while (iterator.hasNext() && (limit < 0 || page.size() < limit)) {
            ExtensionJobHistoryRecord record = iterator.next();
            if (filter.evaluate(record)) {
                page.add(record);
            }
        }

        return page;
    }

    private void save(ExtensionJobHistoryRecord record)
    {
        try {
            this.serializer.append(record, new File(this.config.getStorage(), getFileName()));
        } catch (IOException e) {
            this.logger.error("Failed to save extension job history.", e);
        }
    }

    private String getFileName()
    {
        return FILE_NAME_FORMAT.format(new Date());
    }

    private void load()
    {
        for (File historyFile : getHistoryFiles()) {
            try {
                List<ExtensionJobHistoryRecord> storedRecords = this.serializer.read(historyFile);
                Collections.reverse(storedRecords);
                this.records.addAll(storedRecords);
            } catch (Exception e) {
                this.logger.error("Failed to read extension job history from [{}].", historyFile.getAbsolutePath(), e);
            }
        }
    }

    private List<File> getHistoryFiles()
    {
        File[] files = this.config.getStorage().listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                return file.isFile() && file.getName().endsWith(".xml");
            }
        });
        List<File> fileList = files != null ? Arrays.asList(files) : Collections.<File>emptyList();
        Collections.sort(fileList, Collections.reverseOrder());
        return fileList;
    }
}
