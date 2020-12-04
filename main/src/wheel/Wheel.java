package wheel;

import java.util.ArrayList;
import java.util.List;

public final class Wheel<T> {
    protected final int lenght;
    private final List<T>[] wheel;

    private int index = 0;

    private final long bucketSize;
    private final long maxTimeout;

    public Wheel(int length, long tickDurationInMillis) {
        this.lenght = length;
        this.bucketSize = tickDurationInMillis;
        this.maxTimeout = length * tickDurationInMillis;
        if (length <= 0)
            throw new IllegalArgumentException("Can not create a wheel with size = " + length);

        wheel = new ArrayList[length];
        for (int i = 0; i < length; i++) {
            wheel[i] = new ArrayList<>();
        }

    }

    public long getBucketTime() {
        return bucketSize;
    }

    public long getMaxTimeout() {
        return maxTimeout;
    }

    public void add(T item, int bucket) {
        wheel[(bucket + index) % lenght].add(item);
    }

    public List<T> nextBucket() {
        index = ++index % lenght;

        List<T> result = new ArrayList<>(wheel[index]);
        wheel[index].clear();
        return result;
    }

    public boolean cascade() {
        return index == 0;
    }

    public int remainingTick() {
        return lenght-index;
    }
}
