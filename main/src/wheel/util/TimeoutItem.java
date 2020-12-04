package wheel.util;

import java.time.Duration;

public class TimeoutItem {

    private final TimeOutable item;
    private long timeoutMillis;


    public TimeoutItem(TimeOutable item, Duration timeout) {
        this.item = item;
        this.timeoutMillis = timeout.toMillis();
    }

    public void updateTimeout(long newTimeout) {
        timeoutMillis = newTimeout;
    }

    public TimeOutable getItem() {
        return item;
    }

    public long getTimeoutInMillis() {
        return timeoutMillis;
    }
}
