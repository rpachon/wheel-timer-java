package wheel;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by stan on 08/10/16.
 */

@RunWith(DataProviderRunner.class)
public class WheelTest {

    @Test
    public void should_create_wheel_time() {
        // Given

        // When
        Wheel wheelTimer = new Wheel(256);

        // Then
        assertThat(wheelTimer).isNotNull();
    }

    @Test
    @DataProvider(value = {
            "-1",
            "0"
    })
    public void should_throw_exception_if_size_is_less_or_equals_0(String value) {
        // Given

        // When Then
        assertThatThrownBy(() -> {
            new Wheel(Integer.valueOf(value));
        }).hasMessage("Can not create a wheel with size = %s", value);
    }

    @Test
    public void should_add_item_of_different_types_in_bucket() {
        // Given
        Wheel<Long> longWheelTimer = new Wheel(256);
        Wheel<String> stringWheelTimer = new Wheel(256);

        // When
        longWheelTimer.add(Long.valueOf(56), 5);
        stringWheelTimer.add("test", 10);

        // Then
        goForwardFrom(longWheelTimer, 4);
        assertThat(longWheelTimer.nextBucket().get(0)).isEqualTo(Long.valueOf(56));
        goForwardFrom(stringWheelTimer, 9);
        assertThat(stringWheelTimer.nextBucket().get(0)).isEqualTo("test");
    }

    private void goForwardFrom(Wheel<?> longWheelTimer, int bucketNumber) {
        for (int i = 0; i < bucketNumber; i++) {
            longWheelTimer.nextBucket();
        }
    }

    @Test
    public void should_add_many_items_in_the_same_bucket() {
        // Given
        Wheel<Long> wheelTimer = new Wheel<>(256);

        // When
        wheelTimer.add(4l, 3);
        wheelTimer.add(5l, 3);

        // Then
        goForwardFrom(wheelTimer, 2);
        List<Long> expectedList = wheelTimer.nextBucket();
        assertThat(expectedList.size()).isEqualTo(2);
        assertThat(expectedList.get(0)).isEqualTo(4l);
        assertThat(expectedList.get(1)).isEqualTo(5l);
    }

    @Test
    public void should_get_next_bucket() {
        // Given
        Wheel<String> wheelTimer = new Wheel<>(5);
        wheelTimer.add("test0", 0);
        wheelTimer.add("test1", 1);
        wheelTimer.add("test2", 2);
        wheelTimer.add("test3", 3);
        wheelTimer.add("test4", 4);
        wheelTimer.add("test5", 4);


        // When
        List<String> result =  wheelTimer.nextBucket();
        // Then
        assertThat(result).containsExactly("test1");

        // When
        result =  wheelTimer.nextBucket();
        // Then
        assertThat(result).containsExactly("test2");

        // When
        result =  wheelTimer.nextBucket();
        // Then
        assertThat(result).containsExactly("test3");

        // When
        result =  wheelTimer.nextBucket();
        // Then
        assertThat(result).containsExactly("test4", "test5");

        // When
        result =  wheelTimer.nextBucket();
        // Then
        assertThat(result).containsExactly("test0");
    }

    @Test
    public void should_rotate_when_getting_more_than_size_next_bucket() {
        // Given
        Wheel<String> wheelTimer = new Wheel<>(5);
        for (int i = 0; i < 4; i++) {
            wheelTimer.nextBucket();
        }
        wheelTimer.add("test", 2);
        wheelTimer.nextBucket();

        // When
        List<String> expectedList = wheelTimer.nextBucket();

        // Then
        assertThat(expectedList).containsExactly("test");
    }

    @Test
    public void should_clear_bucket_when_bucket_is_ridden() {
        // Given
        Wheel<String> wheelTimer = new Wheel<>(5);
        wheelTimer.add("test", 2);


        // When Then
        for (int i=0; i<10; i++) {
            List<String> expectedList = wheelTimer.nextBucket();
            if (i==1) assertThat(expectedList).containsExactly("test");
            else assertThat(expectedList).isEmpty();
        }
    }

    @Test
    public void should_cascade_when_making_complete_turn() {
        // Given
        Wheel<String> wheelTimer = new Wheel<>(5);

        // When
        goForwardFrom(wheelTimer, 5);

        // Then
        assertThat(wheelTimer.cascade()).isTrue();
    }

}