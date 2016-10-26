package wheel;

import com.sun.org.apache.xpath.internal.SourceTree;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import wheel.util.TimeOutable;
import wheel.util.Timeout;
import wheel.util.TimeoutItem;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static wheel.TestUtil.getWheelsList;
import static wheel.TestUtil.goForwardFrom;

/**
 * Created by stan on 10/10/16.
 */
@RunWith(DataProviderRunner.class)
public class WheelTimerTest {


    @Test
    @DataProvider( value={
            "148|1",
            "11000|2",
            "120000|3",
            "2000000|4"
    }, splitBy = "\\|", trimValues = true)
    public void should_create_wheels_function_of_max_timeout(String timeout, String wheelNumber) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(Long.valueOf(timeout), TimeUnit.MILLISECONDS);

        // When
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);

        // Then
        List<Wheel<TimeoutItem>> wheelsNumber = getWheelsList(timer);
        assertThat(wheelsNumber).hasSize(Integer.valueOf(wheelNumber));
    }

    @Test
    @DataProvider( value={
            "1",
            "2",
            "10",
            "125",
            "256"
    })
    public void should_add_an_item_in_the_first_wheel_when_timeout_between_1_and_256_multiply_tick_duration(long timeout) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        TimeOutable timeOutable = mock(TimeOutable.class);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(timeout, TimeUnit.MILLISECONDS));

        // When
        timer.add(item);

        // Then
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(0), (int) (timeout-1));
        assertThat(wheels.get(0).nextBucket()).containsExactly(item);
    }

    @Test
    @DataProvider( value={
            "257|1",
            "16000|62",
            "16384|64",
            "16384|64"
    }, splitBy = "\\|", trimValues = true)
    public void should_add_an_item_in_the_second_wheel_when_timeout_between_257_and_16384_multiply_tick_duration(long timeout, int bucket) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        TimeOutable timeOutable = mock(TimeOutable.class);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(timeout, TimeUnit.MILLISECONDS));

        // When
        timer.add(item);

        // Then
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(1), bucket-1);
        assertThat(wheels.get(1).nextBucket()).containsExactly(item);
    }

    @Test
    @DataProvider( value={
            "16641|0",
            "120000|7",
            "1048576|64"
    }, splitBy = "\\|", trimValues = true)
    public void should_add_an_item_in_the_third_wheel_timeout_between_16641_and_1065216_multiply_tick_duration(long timeout, int bucket) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        TimeOutable timeOutable = mock(TimeOutable.class);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(timeout, TimeUnit.MILLISECONDS));

        // When
        timer.add(item);

        // Then
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(2), bucket-1);
        assertThat(wheels.get(2).nextBucket()).containsExactly(item);
    }

    @Test
    @DataProvider( value={
            "1",
            "2",
            "10",
            "125",
            "230",
            "250",
            "256"
    })
    public void should_add_an_item_in_first_wheel_when_wheel_has_already_tick(long timeout) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(0), 120);
        TimeOutable timeOutable = mock(TimeOutable.class);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(timeout, TimeUnit.MILLISECONDS));

        // When
        timer.add(item);

        // Then
        goForwardFrom(wheels.get(0), (int) timeout-1);
        assertThat(wheels.get(0).nextBucket()).containsExactly(item);
    }

    @Test
    public void should_add_an_item_in_second_wheel_when_wheels_has_already_tick() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(1), 60);
        TimeOutable timeOutable = mock(TimeOutable.class);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(15000, TimeUnit.MILLISECONDS));

        // When
        timer.add(item);

        // Then
        List<Wheel<TimeoutItem>> wheelsToTest = getWheelsList(timer);
        goForwardFrom(wheelsToTest.get(1), 57);
        assertThat(wheels.get(1).nextBucket()).containsExactly(item);
    }

    @Test
    public void should_add_an_item_in_third_wheel_when_wheels_has_already_tick() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(2), 10);
        TimeOutable timeOutable = mock(TimeOutable.class);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(1_000_000, TimeUnit.MILLISECONDS));

        // When
        timer.add(item);

        // Then
        List<Wheel<TimeoutItem>> wheelsToTest = getWheelsList(timer);
        goForwardFrom(wheelsToTest.get(2), 60);
        assertThat(wheels.get(2).nextBucket()).containsExactly(item);
    }

    @Test
    public void should_compute_remaining_time_when_adding_in_wheels() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        TimeOutable timeOutable = mock(TimeOutable.class);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(1, TimeUnit.MINUTES));

        // When
        timer.add(item);

        // Then
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(2), 2);
        List<TimeoutItem> expectedItem = wheels.get(2).nextBucket();
        assertThat(expectedItem.get(0).getTimeout().value).isEqualTo(10848);
        assertThat(expectedItem.get(0).getTimeout().unit).isEqualTo(TimeUnit.MILLISECONDS);
    }

    @Test
    @DataProvider( value={
            "1",
            "2",
            "10",
            "125",
            "256",
            "257",
            "16000",
            "16384",
            "16640",
            "16641",
            "100000",
            "120000"
    })
    public void should_cascade_bucket_if_not_in_the_first_wheel(int timeout) {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(120_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        TimeOutable timeOutable = mock(TimeOutable.class);
        given(timeOutable.isRunning()).willReturn(true);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(Long.valueOf(timeout), TimeUnit.MILLISECONDS));
        timer.add(item);


        for (int i=1; i<1064965; i++) {
            // When
            timer.tick();

            // Then
            if (i == timeout) {
                verify(timeOutable).timeout();
            }
        }
    }

    @Test
    @DataProvider( value={
            "1",
            "2",
            "10",
            "125",
            "256",
            "257",
            "16000",
            "16384",
            "16640",
            "16641",
            "100000",
            "120000",
            "1048570"
    })
    public void should_timeout_all_items_with_specific_number_of_tick_and_wheels_are_already_running(long value) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(500_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        List<Wheel<TimeoutItem>> wheels = getWheelsList(timer);
        goForwardFrom(wheels.get(0), 200);
        goForwardFrom(wheels.get(1), 50);
        goForwardFrom(wheels.get(2), 2);


        TimeOutable timeOutable = mock(TimeOutable.class);
        given(timeOutable.isRunning()).willReturn(true);
        TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(value, TimeUnit.MILLISECONDS));
        timer.add(item);


        // When
        for (int i=0; i<value-1; i++) {
            timer.tick();
        }

        // Then
        verify(timeOutable, never()).timeout();

        // When
        timer.tick();

        // Then
        verify(timeOutable).timeout();

    }


    //Really long test
    @Ignore
    @Test
    public void should_timeout_all_items() {
        // Given
        Timeout tickDuration = new Timeout(1, TimeUnit.MILLISECONDS);
        Timeout maxTimeout = new Timeout(500_000, TimeUnit.MILLISECONDS);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        TimeOutable[] listTimeoutable = new TimeOutable[500_000];
        for (int i=1; i<500000; i++) {
            TimeOutable timeOutable = mock(TimeOutable.class);
            given(timeOutable.isRunning()).willReturn(true);
            listTimeoutable[i] = timeOutable;
            TimeoutItem item = new TimeoutItem(timeOutable, new Timeout(i, TimeUnit.MILLISECONDS));
            timer.add(item);
        }

        for (int i=1; i<500000; i++) {

            // When
            timer.tick();

            // Then
            verify(listTimeoutable[i]).timeout();
        }
    }
}