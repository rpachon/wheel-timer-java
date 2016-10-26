package wheel;

import wheel.util.Timeout;
import wheel.util.TimeoutItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class WheelTimer {

    private final int FIRST_WHEEL_SIZE = 256;
    private final int OTHER_WHEEL_SIZE = 64;

    private final List<Wheel<TimeoutItem>> wheels;
    private final long tickDurationInMillis;
    private final Timer timer = new Timer(true);

    public WheelTimer(Timeout tickDuration, Timeout maxTimeout) {
        this.tickDurationInMillis = TimeUnit.MILLISECONDS.convert(tickDuration.value, tickDuration.unit);
        long maxTimeoutInMillis = TimeUnit.MILLISECONDS.convert(maxTimeout.value, maxTimeout.unit);

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
        long timeoutValueInMillis = TimeUnit.MILLISECONDS.convert(timeoutItem.getTimeout().value, timeoutItem.getTimeout().unit);
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

    protected void tick() {
        for (int i = 0; i < wheels.size(); i++) {
            cascade(wheels.get(i).nextBucket());
            if (!wheels.get(i).cascade()) {
                break;
            }
        }
    }

    private void cascade(List<TimeoutItem> timeoutItems) {
        for (TimeoutItem timeoutItem : timeoutItems) {
            if (timeoutItem.item.isRunning()) {
                if (timeoutItem.getTimeout().value == 0) {
                    timeoutItem.item.timeout();
                } else {
                    add(timeoutItem);
                }
            }
        }
    }
}
