package wheel.benchmark;

import net.openhft.chronicle.core.jlbh.JLBH;
import net.openhft.chronicle.core.jlbh.JLBHOptions;
import net.openhft.chronicle.core.jlbh.JLBHTask;
import wheel.WheelTimer;
import wheel.util.TimeOutable;
import wheel.util.Timeout;
import wheel.util.TimeoutItem;

import java.util.concurrent.TimeUnit;

/**
 * Created by stan on 13/10/16.
 */
public class BenchmarkTimeout implements JLBHTask {

    public static final int WARMUP_ITERATION = 10;
    public static final int THROUGHPUT = 1_000_000;
    public static final int SECOND_NUMBER = 10;
    public static final int RUNS = 5;
    private WheelTimer timer;
    private Timeout timeout;
    JLBH jlbh;

    public static void main(String[] args){
        JLBHOptions lth = new JLBHOptions()
                .warmUpIterations(WARMUP_ITERATION)
                .iterations(THROUGHPUT*SECOND_NUMBER)
                .throughput(THROUGHPUT)
                .runs(RUNS)
                .recordOSJitter(true)
                .accountForCoordinatedOmmission(true)
                .jlbhTask(new BenchmarkTimeout());
        new JLBH(lth).start();
    }

    @Override
    public void init(JLBH jlbh) {
        this.jlbh = jlbh;
        timer = new WheelTimer(new Timeout(5, TimeUnit.MILLISECONDS),
                new Timeout(2, TimeUnit.SECONDS));
        timer.start();
        timeout = new Timeout(1, TimeUnit.SECONDS);
    }

    @Override
    public void run(long startTimeNS) {
        TimeoutItem item = new TimeoutItem(new Item(startTimeNS), timeout);
        timer.add(item);
    }

    @Override
    public void complete() {

    }

    private class Item implements TimeOutable {

        public final long start;

        private Item(long start) {
            this.start = start;
        }

        @Override
        public void timeout() {
            long end = System.nanoTime();
            jlbh.sample(end-start);
        }

        @Override
        public boolean isRunning() {
            return true;
        }
    }
}