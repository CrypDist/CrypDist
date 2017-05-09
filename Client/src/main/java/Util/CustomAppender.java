package Util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.pattern.LogEvent;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by od on 8.05.2017.
 */
public class CustomAppender extends AppenderSkeleton {
    public static ArrayList<LoggingEvent> eventsList = new ArrayList();

    @Override
    protected void append(LoggingEvent event) {
        eventsList.add(event);
    }

    public void close() {

    }


    public boolean requiresLayout() {
        return false;
    }

}
