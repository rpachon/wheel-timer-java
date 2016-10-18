package wheel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Wheel<T> {
    protected final int lenght;
    private final List<T>[] wheel;

    private volatile AtomicInteger index = new AtomicInteger(0);
    private volatile boolean cascade;

    private ReentrantLock lock = new ReentrantLock(true);

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
        try {
            lock.lock();
            wheel[(bucket+index.get())%lenght].add(item);
        } finally {
            lock.unlock();
        }
    }

    public List<T> nextBucket() {
        cascade = false;
        if (index.incrementAndGet() == lenght) {
            index.set(0);
            cascade = true;
        }
        List<T> result;
        try {
            lock.lock();
            result = new ArrayList<T>(wheel[index.get()]);
            wheel[index.get()].clear();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public boolean cascade() {
        return cascade;
    }

}
