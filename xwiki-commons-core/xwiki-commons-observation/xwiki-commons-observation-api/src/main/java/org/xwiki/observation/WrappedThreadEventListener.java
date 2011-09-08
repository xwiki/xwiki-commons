package org.xwiki.observation;

import java.util.List;

import org.xwiki.observation.event.Event;

/**
 * Wrap a provide listener and filter received events coming from provided {@link Thread}.
 * 
 * @version $Id$
 * @since 3.2M3
 */
public class WrappedThreadEventListener extends AbstractThreadEventListener
{
    /**
     * The wrapped listener.
     */
    private EventListener listener;

    /**
     * @param listener the wrapped listener
     * @param thread the thread to match to receive events
     */
    public WrappedThreadEventListener(EventListener listener, Thread thread)
    {
        super(thread);

        this.listener = listener;
    }

    @Override
    public List<Event> getEvents()
    {
        return this.listener.getEvents();
    }

    @Override
    public String getName()
    {
        return this.listener.getName();
    }

    @Override
    protected void onEventInternal(Event event, Object source, Object data)
    {
        this.listener.onEvent(event, source, data);
    }
}
