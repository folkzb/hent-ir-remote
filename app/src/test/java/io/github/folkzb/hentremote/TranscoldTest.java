// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;
import org.junit.Test;
import static org.junit.Assert.*;

public class TranscoldTest {
    @Test public void matchesUpstreamRecordedState() {
        // Actual M1-F-NO-6 recording in upstream ir_Transcold_test.cpp.
        assertEquals(0xE96554,Transcold.state(22,Transcold.COOL,1));
        assertEquals(0xEF6154,Transcold.state(24,Transcold.COOL,0));
        assertEquals(0xEF7954,Transcold.OFF);
    }
    @Test public void matchesUpstreamGoldenWaveform() {
        String expected = "111010010001011001100101100110100101010010101011";
        int[] frame = Transcold.pattern(0xE96554);
        assertEquals(102, frame.length); assertEquals(5944,frame[0]); assertEquals(7563,frame[1]);
        for (int i=0;i<48;i++) {
            assertEquals(555,frame[2+i*2]);
            assertEquals(expected.charAt(i)=='1' ? 3556 : 1526,frame[3+i*2]);
        }
        assertArrayEquals(new int[]{555,7563,555,100000},java.util.Arrays.copyOfRange(frame,98,102));
        new IrSignal(38000,frame);
    }
    @Test public void matchesTemperatureExamplesFromUpstream() {
        int[] expected = {0xEF6754,0xEF6B54,0xEF6354,0xEF6D54,0xEF6554,0xEF6954,0xEF6154,
            0xEF6E54,0xEF6654,0xEF6A54,0xEF6254,0xEF6C54,0xEF6454};
        for(int t=18;t<=30;t++) assertEquals(expected[t-18],Transcold.state(t,Transcold.COOL,0));
    }
    @Test public void supportsFanAndAutoVariants() {
        assertEquals(0xEFcf54,Transcold.state(24,Transcold.FAN,0));
        assertEquals(0xE6e154,Transcold.state(24,Transcold.AUTO,0));
        assertEquals(0xED6154,Transcold.state(24,Transcold.COOL,2));
        assertEquals(0xEB6154,Transcold.state(24,Transcold.COOL,3));
    }
    @Test public void allModesAndTemperaturesStayWithinAndroidLimit() {
        for(int mode : new int[]{Transcold.COOL,Transcold.AUTO,Transcold.FAN})
            for(int t=18;t<=30;t++) for(int fan=0;fan<=3;fan++)
                new IrSignal(38000,Transcold.pattern(Transcold.state(t,mode,fan)));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnsupportedTemperature() { Transcold.state(16,6,0); }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidSpeed() { Transcold.state(24,6,4); }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidMode() { Transcold.state(24,99,0); }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidCode() { Transcold.pattern(0x1000000); }
}
