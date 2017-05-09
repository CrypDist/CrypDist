package Util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by od on 8.05.2017.
 */
public class CustomAppender extends AppenderSkeleton {
    public static LinkedBlockingQueue<LoggingEvent> eventsList = new LinkedBlockingQueue<>();

    @Override
    protected void append(LoggingEvent event) {
        eventsList.add(event);
    }

    public static void clear() {
        eventsList.clear();
    }

    public static String getMessages() {
        StringBuilder stringBuilder = new StringBuilder();

        for(LoggingEvent event : eventsList) {
            stringBuilder.append(event.getLoggerName() + ": " + event.getRenderedMessage() + "\n");
        }

        clear();

        return stringBuilder.toString();
    }
    public void close() {

    }


    public boolean requiresLayout() {
        return false;
    }

}
