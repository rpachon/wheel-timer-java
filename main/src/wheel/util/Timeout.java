package wheel.util;

import java.util.concurrent.TimeUnit;

/**
 * Created by stan on 10/10/16.
 */
public final class Timeout {

    public final long value;
    public final TimeUnit unit;

    public Timeout(long value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }
}
