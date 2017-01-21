package wheel;

import wheel.util.TimeoutItem;

import java.lang.reflect.Field;
import java.util.List;

public class TestUtil {

    public static void goForwardFrom(Wheel wheel, int tickNumbers) {
        for (int i = 0; i < tickNumbers; i++) {
            wheel.nextBucket();
        }
    }

    public static List<Wheel<TimeoutItem>> getWheelsList(WheelTimer timer) throws NoSuchFieldException, IllegalAccessException {
        Field wheels = WheelTimer.class.getDeclaredField("wheels");
        wheels.setAccessible(true);
        return (List<Wheel<TimeoutItem>>) wheels.get(timer);
    }

}
