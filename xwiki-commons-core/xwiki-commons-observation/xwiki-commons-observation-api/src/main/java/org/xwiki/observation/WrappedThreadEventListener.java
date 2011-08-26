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

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getEvents()
     */
    @Override
    public List<Event> getEvents()
    {
        return this.listener.getEvents();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getName()
     */
    @Override
    public String getName()
    {
        return this.listener.getName();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.AbstractThreadEventListener#onEventInternal(org.xwiki.observation.event.Event,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    protected void onEventInternal(Event event, Object source, Object data)
    {
        this.listener.onEvent(event, source, data);
    }
}
