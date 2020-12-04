package wheel.util;

import java.time.Duration;
import java.util.function.LongSupplier;

public class TimeoutItem {

    private final LongSupplier clock;
    private final TimeOutable item;
    private final long targetTime;


    public TimeoutItem(LongSupplier clock, TimeOutable item, Duration timeout) {
        this.clock = clock;
        this.item = item;
        this.targetTime = clock.getAsLong() + timeout.toMillis();
    }

    public TimeOutable getItem() {
        return item;
    }

    public long getTimeoutInMillis() {
        return targetTime - clock.getAsLong();
    }
}
