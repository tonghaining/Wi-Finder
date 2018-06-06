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
    public void getOptimalAngle_basicTest(){
        MainActivity activity = new MainActivity();

        int result = activity.getOptimalAngle(6,10,17,50,210);
        Assert.assertEquals(result, 141);

        // More realistic values
        int result1 = activity.getOptimalAngle(-64,-60,-53,50,210);
        Assert.assertEquals(result1, 141);
    }

    @Test
    public void getOptimalAngle_0_to_360_angle(){
        MainActivity activity = new MainActivity();

        int result2 = activity.getOptimalAngle(6,10,17,310,150);
        Assert.assertEquals(result2, 219);

        int result3 = activity.getOptimalAngle(0,5,15,90,210);
        Assert.assertEquals(result3, 180);
        int result4 = activity.getOptimalAngle(0,5,15,270,150);
        Assert.assertEquals(result4, 180);
        int result5 = activity.getOptimalAngle(0,5,15,30,270);
        Assert.assertEquals(result5, 300);
        int result6 = activity.getOptimalAngle(0,1732,3732,270,30);
        Assert.assertEquals(result6, 0);

        int result8 = activity.getOptimalAngle(0,1,2,30,150);
        Assert.assertEquals(result8, 120);
    }

    @Test
    public void getOptimalAngle_right_transpose(){
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
    @Test
    public void getOptimalAngle_left_transpose(){
        MainActivity activity = new MainActivity();

        int qudrant_one = activity.getOptimalAngle(6,10,17,210,50);
        Assert.assertEquals(qudrant_one, 119);

        int qudrant_two = activity.getOptimalAngle(10,6,17,210,50);
        Assert.assertEquals(qudrant_two, 29);

        int qudrant_three = activity.getOptimalAngle(6,17,10,210,50);
        Assert.assertEquals(qudrant_three, 210);

        int qudrant_four = activity.getOptimalAngle(10,6,5,210,50);
        Assert.assertEquals(qudrant_four, 300);
    }

    @Test
    public void getOptimalAngle_obtuseAngles_turnRight(){
        MainActivity activity = new MainActivity();

        //Turn right
        int result1 = activity.getOptimalAngle(6,10,17,300,340);
        Assert.assertEquals(result1, 291);
        int result2 = activity.getOptimalAngle(10,6,17,300,340);
        Assert.assertEquals(result2, 21);
        int result3 = activity.getOptimalAngle(6,17,10,300,340);
        Assert.assertEquals(result3, 201);
        int result4 = activity.getOptimalAngle(17,10,6,300,340);
        Assert.assertEquals(result4, 111);
    }

    @Test
    public void getOptimalAngle_obtuseAngles_turnLeft(){
        MainActivity activity = new MainActivity();

        //Turn left
        int result1 = activity.getOptimalAngle(6,10,17,340,300);
        Assert.assertEquals(result1, 349);
        int result2 = activity.getOptimalAngle(10,6,17,340,300);
        Assert.assertEquals(result2, 259);
        int result3 = activity.getOptimalAngle(6,17,10,340,300);
        Assert.assertEquals(result3, 79);
        int result4 = activity.getOptimalAngle(17,10,6,340,300);
        Assert.assertEquals(result4, 169);
    }
    
    @Test
    public void getOptimalAngle_endToEnd(){
        MainActivity activity = new MainActivity();

        // Initial Rssi
        int s1 = 6;
        int s2 = 17;
        int s3 = 10;

        // Initial angles
        int a1 = 210;
        int a2 = 50;

        int firstIterationOptimalAngle = activity.getOptimalAngle(s1, s2, s3, a1, a2);
        Assert.assertEquals(firstIterationOptimalAngle, 210);

        int s4 = 15;

        int secondIterationOptimalAngle = activity.getOptimalAngle(s2, s3, s4, a2, firstIterationOptimalAngle);
        Assert.assertEquals(secondIterationOptimalAngle, 230);

        int s5 = 17;
        int thirdIterationOptimalAngle = activity.getOptimalAngle(s3, s4, s5, firstIterationOptimalAngle, secondIterationOptimalAngle);
        Assert.assertEquals(thirdIterationOptimalAngle, 161);
    }

    @Test
    public void testIter(){
        MainActivity activity = new MainActivity();

        activity.init();
        int opt1 = activity.iteration(210,6);
        Assert.assertEquals(-1, opt1);

        int opt2 = activity.iteration(50,10);
        Assert.assertEquals(-1, opt2);

        int opt3 = activity.iteration(100,17);
        Assert.assertEquals(119, opt3);

        int opt4 = activity.iteration(opt3,7);
        Assert.assertEquals(331, opt4);

    }
}