package wheel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Wheel<T> {
    protected final int lenght;
    private final List<T>[] wheel;

    private int index = 0;
    private boolean cascade;

    public Wheel(int lenght) {
        this.lenght = lenght;
        if (lenght <= 0)
            throw new IllegalArgumentException("Can not create a wheel with size = " + lenght);

        wheel = new ArrayList[lenght];
        for (int i = 0; i < lenght; i++) {
            wheel[i] = new ArrayList<T>();
        }
    }

    public void add(T item, int bucket) {
        wheel[(bucket+index)%lenght].add(item);
    }

    public List<T> nextBucket() {
        cascade = false;
        if (++index == lenght) {
            index = 0;
            cascade = true;
        }
        List<T> result = new ArrayList<T>(wheel[index]);
        wheel[index].clear();
        return result;
    }

    public boolean cascade() {
        return cascade;
    }

    public int remainingTick() {
        return lenght-index;
    }
}
