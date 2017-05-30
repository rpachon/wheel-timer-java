package wheel.util;

import java.util.concurrent.TimeUnit;

public final class Timeout {

    private final long value;
    private final TimeUnit unit;

    public Timeout(long value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public long getValue() {
        return value;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}


