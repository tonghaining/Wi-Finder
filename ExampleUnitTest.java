package mobilecomputing.ku.wifinder;

import android.app.Activity;

import junit.framework.Assert;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getOptimalAngle_basicTest(){
        MainActivity activity = new MainActivity();

        int result = activity.getOptimalAngle(6,10,17,50,210);
        Assert.assertEquals(result, 141);

        // More realistic values
        int result1 = activity.getOptimalAngle(-64,-60,-53,50,210);
        Assert.assertEquals(result1, 141);

        int result2 = activity.getOptimalAngle(6,10,17,310,150);
        Assert.assertEquals(result2, 141);
        int result3 = activity.getOptimalAngle(0,5,15,90,210);
        Assert.assertEquals(result3, 180);
        int result4 = activity.getOptimalAngle(0,5,15,270,150);
        Assert.assertEquals(result4, 180);
    }

    @Test
    public void getOptimalAngle_transpose(){
        MainActivity activity = new MainActivity();

        int qudrant_one = activity.getOptimalAngle(6,10,17,50,210);
        Assert.assertEquals(qudrant_one, 141);

        int qudrant_two = activity.getOptimalAngle(10,6,17,50,210);
        Assert.assertEquals(qudrant_two, 231);

        int qudrant_three = activity.getOptimalAngle(6,17,10,50,210);
        Assert.assertEquals(qudrant_three, 50);

        int qudrant_four = activity.getOptimalAngle(10,6,5,50,210);
        Assert.assertEquals(qudrant_four, 320);
    }
}