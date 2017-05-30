package wheel;

import wheel.util.Timeout;
import wheel.util.TimeoutItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class WheelTimer {

    private final int FIRST_WHEEL_SIZE = 256;
    private final int OTHER_WHEEL_SIZE = 64;

    private final List<Wheel<TimeoutItem>> wheels;
    private final long tickDurationInMillis;
    private final Timer timer = new Timer(true);

    private ReentrantLock lock = new ReentrantLock(true);


    public WheelTimer(Timeout tickDuration, Timeout maxTimeout) {
        this.tickDurationInMillis = TimeUnit.MILLISECONDS.convert(tickDuration.getValue(), tickDuration.getUnit());
        long maxTimeoutInMillis = TimeUnit.MILLISECONDS.convert(maxTimeout.getValue(), maxTimeout.getUnit());

        int wheelNumber = computeWheelsNumber(maxTimeoutInMillis);
        wheels = new ArrayList<>(wheelNumber);
        createWheels(wheelNumber);
    }

    private void createWheels(int wheelNumber) {
        wheels.add(new Wheel(FIRST_WHEEL_SIZE));
        for (int i = 1; i < wheelNumber; i++) {
            wheels.add(new Wheel(OTHER_WHEEL_SIZE));
        }
    }

    private int computeWheelsNumber(long maxTimeoutInMillis) {
        int wheelNumber = 0;
        long timePerWheel = FIRST_WHEEL_SIZE * this.tickDurationInMillis;
        while(maxTimeoutInMillis > 0) {
            maxTimeoutInMillis -= timePerWheel * Math.pow(OTHER_WHEEL_SIZE, wheelNumber++);
        }
        return wheelNumber;
    }

    public void start() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        }, 0, tickDurationInMillis);
    }

    public void  add(TimeoutItem timeoutItem) {
        try {
            lock.lock();
            computeAndAdd(timeoutItem);
        } finally {
            lock.unlock();
        }
    }

    private void  computeAndAdd(TimeoutItem timeoutItem) {
        long timeoutValueInMillis = TimeUnit.MILLISECONDS.convert(timeoutItem.getTimeout().getValue(), timeoutItem.getTimeout().getUnit());
        long currentWheelTime = FIRST_WHEEL_SIZE * tickDurationInMillis;
        long bucketDuration = tickDurationInMillis;
        timeoutValueInMillis -= bucketDuration;
        for (int i = 0; i < wheels.size(); i++) {
            if (timeoutValueInMillis < currentWheelTime) {
                int bucket = (int) (timeoutValueInMillis / bucketDuration);
                timeoutItem.updateTimeout(new Timeout(timeoutValueInMillis - (bucket * bucketDuration), TimeUnit.MILLISECONDS));
                wheels.get(i).add(timeoutItem, bucket + 1);
                break;
            }
            timeoutValueInMillis -= (wheels.get(i).remainingTick()-1) * bucketDuration;
            bucketDuration = currentWheelTime;
            currentWheelTime *= OTHER_WHEEL_SIZE;
        }
    }

    private void tick() {
        try {
            lock.lock();
            for (int i = 0; i < wheels.size(); i++) {
                cascade(wheels.get(i).nextBucket());
                if (!wheels.get(i).cascade()) {
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void cascade(List<TimeoutItem> timeoutItems) {
        for (TimeoutItem timeoutItem : timeoutItems) {
            if (timeoutItem.getItem().isRunning()) {
                if (timeoutItem.getTimeout().getValue() < tickDurationInMillis) {
                    timeoutItem.getItem().timeout();
                } else {
                    computeAndAdd(timeoutItem);
                }
            }
        }
    }
}
