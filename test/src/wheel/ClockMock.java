package wheel;

import java.time.Duration;
import java.util.function.LongSupplier;

public class ClockMock implements LongSupplier {

    private final long tickDuration;
    private long time = 0;

    public ClockMock(Duration tickDuration) {
        this.tickDuration = tickDuration.toMillis();
    }

    public void tick() {
        time += tickDuration;
    }

    @Override
    public long getAsLong() {
        return time;
    }
}
