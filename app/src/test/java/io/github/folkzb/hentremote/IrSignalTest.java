package io.github.folkzb.hentremote;
import org.junit.Test;
import static org.junit.Assert.*;
public class IrSignalTest {
    @Test public void signalIsImmutable() {
        int[] pulses = {9000,4500,560,560}; IrSignal s = new IrSignal(38000,pulses);
        pulses[0]=0; int[] copy=s.patternUs(); copy[0]=1; assertEquals(9000,s.patternUs()[0]);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsZeroPulse() { new IrSignal(38000,new int[]{0,1}); }
    @Test(expected=IllegalArgumentException.class) public void rejectsNegativePulse() { new IrSignal(38000,new int[]{1,-1}); }
    @Test(expected=IllegalArgumentException.class) public void rejectsLongSignal() { new IrSignal(38000,new int[]{1000000,1000000}); }
    @Test(expected=IllegalArgumentException.class) public void rejectsOverflowSignal() { new IrSignal(38000,new int[]{Integer.MAX_VALUE,Integer.MAX_VALUE}); }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidCarrier() { new IrSignal(0,new int[]{560,560}); }
    @Test(expected=IllegalArgumentException.class) public void rejectsExcessivePulseCount() { new IrSignal(38000,new int[4097]); }
}
