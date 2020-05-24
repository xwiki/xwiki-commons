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
package org.xwiki.observation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xwiki.observation.event.AllEvent;

/**
 * Unit tests for {@link WrappedThreadEventListener}.
 *
 * @version $Id$
 */
public class WrappedThreadEventListenerTest
{
    private EventListener listenermock = Mockito.mock(EventListener.class);

    @Test
    void testWrapp()
    {
        WrappedThreadEventListener wrapper = new WrappedThreadEventListener(this.listenermock);

        wrapper.getName();
        Mockito.verify(this.listenermock).getName();

        wrapper.getEvents();
        Mockito.verify(this.listenermock).getEvents();
    }

    @Test
    void testOnEventOnCurrentThread() throws InterruptedException
    {
        final WrappedThreadEventListener wrapper = new WrappedThreadEventListener(this.listenermock);

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                wrapper.onEvent(AllEvent.ALLEVENT, null, null);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
        Mockito.verify(this.listenermock, Mockito.times(0)).onEvent(AllEvent.ALLEVENT, null, null);

        wrapper.onEvent(AllEvent.ALLEVENT, null, null);
        Mockito.verify(this.listenermock).onEvent(AllEvent.ALLEVENT, null, null);

        thread = new Thread(runnable);
        thread.start();
        thread.join();
        Mockito.verify(this.listenermock).onEvent(AllEvent.ALLEVENT, null, null);
    }

    @Test
    void testOnEventOnPassedThread() throws InterruptedException
    {
        final WrappedThreadEventListener wrapper =
            new WrappedThreadEventListener(this.listenermock, Thread.currentThread());

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                wrapper.onEvent(AllEvent.ALLEVENT, null, null);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
        Mockito.verify(this.listenermock, Mockito.times(0)).onEvent(AllEvent.ALLEVENT, null, null);

        wrapper.onEvent(AllEvent.ALLEVENT, null, null);
        Mockito.verify(this.listenermock).onEvent(AllEvent.ALLEVENT, null, null);

        thread = new Thread(runnable);
        thread.start();
        thread.join();
        Mockito.verify(this.listenermock).onEvent(AllEvent.ALLEVENT, null, null);
    }
}
