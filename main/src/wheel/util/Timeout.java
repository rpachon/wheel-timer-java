package wheel.util;

import java.util.concurrent.TimeUnit;

public final class Timeout {

    public final long value;
    public final TimeUnit unit;

    public Timeout(long value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }
}
