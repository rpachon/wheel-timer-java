package wheel;

import wheel.util.TimeoutItem;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.StampedLock;


public final class WheelTimer {

    private final int FIRST_WHEEL_SIZE = 256;
    private final int OTHER_WHEEL_SIZE = 64;

    private final List<Wheel<TimeoutItem>> wheels;
    private final long tickDurationInMillis;
    private final Timer timer = new Timer(true);

    private final StampedLock lock = new StampedLock();


    public WheelTimer(Duration tickDuration, Duration maxTimeout) {
        this.tickDurationInMillis = tickDuration.toMillis();
        long maxTimeoutInMillis = maxTimeout.toMillis();

        int wheelNumber = computeWheelsNumber(maxTimeoutInMillis);
        wheels = new ArrayList<>(wheelNumber);
        createWheels(wheelNumber);
    }

    private void createWheels(int wheelNumber) {
        wheels.add(new Wheel(FIRST_WHEEL_SIZE, tickDurationInMillis));
        long tickDuration = tickDurationInMillis * FIRST_WHEEL_SIZE;
        for (int i = 1; i < wheelNumber; i++) {
            wheels.add(new Wheel(OTHER_WHEEL_SIZE, tickDuration));
            tickDuration *= OTHER_WHEEL_SIZE;
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
        long stamp = lock.writeLock();
        try {
            computeAndAdd(timeoutItem);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void  computeAndAdd(TimeoutItem timeoutItem) {
        long timeoutValueInMillis = timeoutItem.getTimeoutInMillis();
        timeoutValueInMillis -= wheels.get(0).getBucketTime();
        for (int i = 0; i < wheels.size(); i++) {
            long bucketTime = wheels.get(i).getBucketTime();
            if (timeoutValueInMillis < wheels.get(i).getMaxTimeout()) {
                int bucket = (int) (timeoutValueInMillis / bucketTime);
                timeoutItem.updateTimeout(timeoutValueInMillis - (bucket * bucketTime));
                wheels.get(i).add(timeoutItem, bucket + 1);
                break;
            }
            timeoutValueInMillis -= (wheels.get(i).remainingTick() - 1) * bucketTime;
        }
    }

    private void tick() {
        long stamp = lock.writeLock();
        try {
            for (int i = 0; i < wheels.size(); i++) {
                cascade(wheels.get(i).nextBucket());
                if (!wheels.get(i).cascade()) {
                    break;
                }
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void cascade(List<TimeoutItem> timeoutItems) {
        for (TimeoutItem timeoutItem : timeoutItems) {
            if (timeoutItem.getItem().isRunning()) {
                if (timeoutItem.getTimeoutInMillis() < tickDurationInMillis) {
                    timeoutItem.getItem().timeout();
                } else {
                    computeAndAdd(timeoutItem);
                }
            }
        }
    }
}
