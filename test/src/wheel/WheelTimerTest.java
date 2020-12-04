package wheel;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import wheel.util.TimeOutable;
import wheel.util.TimeoutItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(DataProviderRunner.class)
public class WheelTimerTest {


    private Method tick;

    @Before
    public void setUp() throws NoSuchMethodException {
        tick = WheelTimer.class.getDeclaredMethod("tick");
        tick.setAccessible(true);
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
    public void should_timeout_all_items_with_tick_duration_at_one_ms(long timeout) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        // Given
        Duration tickDuration = Duration.ofMillis(1);
        Duration maxTimeout = Duration.ofMinutes(18);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);
        for (int i = 0; i < 1000000; ++i) {
            tick.invoke(timer);
        }

        TimeOutable timeOutable = mock(TimeOutable.class);
        given(timeOutable.isRunning()).willReturn(true);
        TimeoutItem item = new TimeoutItem(timeOutable, Duration.ofMillis(timeout));
        timer.add(item);

        // When
        for (int i=0; i<timeout-1; i++) {
            tick.invoke(timer);
        }

        // Then
        verify(timeOutable, never()).timeout();

        // When
        tick.invoke(timer);

        // Then
        verify(timeOutable).timeout();

    }

    @Test
    @DataProvider( value={
            "32, 1",
            "75, 2",
            "7680, 256",
            "7709, 256",
            "7710, 257",
            "125000, 4166"
    })
    public void should_timeout_all_items_with_tick_duration_at_thirty_ms(long timeout, long numberOfTick) throws InvocationTargetException, IllegalAccessException {
        // Given
        Duration tickDuration = Duration.ofMillis(30);
        Duration maxTimeout = Duration.ofMinutes(5);
        WheelTimer timer = new WheelTimer(tickDuration, maxTimeout);

        TimeOutable timeOutable = mock(TimeOutable.class);
        given(timeOutable.isRunning()).willReturn(true);
        TimeoutItem item = new TimeoutItem(timeOutable, Duration.ofMillis(timeout));
        timer.add(item);

        // When
        for (int i = 0; i < numberOfTick - 1; i++) {
            tick.invoke(timer);
        }

        // Then
        verify(timeOutable, never()).timeout();

        // When
        tick.invoke(timer);

        // Then
        verify(timeOutable).timeout();
    }

}