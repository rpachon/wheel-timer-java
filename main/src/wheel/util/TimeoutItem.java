package wheel.util;

import java.sql.Time;

/**
 * Created by stan on 11/10/16.
 */
public class TimeoutItem {

    public final TimeOutable item;
    private Timeout timeout;


    public TimeoutItem(TimeOutable item, Timeout timeout) {
        this.item = item;
        this.timeout = timeout;
    }

    public void updateTimeout(Timeout newTimeout) {
        timeout = newTimeout;
    }

    public Timeout getTimeout() {
        return timeout;
    }
}
