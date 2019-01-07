package com.example.jakesetton.myfirstapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.example.jakesetton.myfirstapp.Alarm;

public class AlarmTest {
    Alarm alarm;

    @Test
    public void testInputStandardise() {
        float[] actual = alarm.inputstandardise(new float[]{(float) 87568.1687, (float) 10.1793, (float) 2432.6346, (float) 102.4264, (float) 7.3694, (float) 302.2001, (float) 33333.0587, (float) 3955.2129});
        float[] expected = new float[]{(float) 1, (float) 1, (float) 1, (float) 1, (float) 1, (float) 1, (float) 1, (float) 1};
        assertArrayEquals(expected, actual, (float) 0.001);
    }

}
