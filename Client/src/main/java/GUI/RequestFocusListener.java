package GUI;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Created by gizem on 12.04.2017.
 */
class RequestFocusListener implements AncestorListener
{
    private boolean removeListener;

    /*
     *  Convenience constructor. The listener is only used once and then it is
     *  removed from the component.
     */
    public RequestFocusListener()
    {
        this(true);
    }

    /*
     *  Constructor that controls whether this listen can be used once or
     *  multiple times.
     *
     *  @param removeListener when true this listener is only invoked once
     *                        otherwise it can be invoked multiple times.
     */
    public RequestFocusListener(boolean removeListener)
    {
        this.removeListener = removeListener;
    }

    @Override
    public void ancestorAdded(AncestorEvent e)
    {
        JComponent component = e.getComponent();
        component.requestFocusInWindow();

        if (removeListener)
            component.removeAncestorListener(this);
    }

    @Override
    public void ancestorMoved(AncestorEvent e) {}

    @Override
    public void ancestorRemoved(AncestorEvent e) {}
}