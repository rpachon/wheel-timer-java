package wheel.util;

public class TimeoutItem {

    public final TimeOutable item;
    private volatile Timeout timeout;


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
