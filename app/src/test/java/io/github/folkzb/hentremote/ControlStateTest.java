package io.github.folkzb.hentremote;
import org.junit.Test;
import static org.junit.Assert.*;
public class ControlStateTest {
    @Test public void powerTogglesWithoutAConfirmationDialog() {
        ControlState initial=new ControlState(24,0,Transcold.COOL,false);
        ControlState on=initial.next(Command.POWER),off=on.next(Command.POWER);
        assertFalse(initial.power);assertTrue(on.power);assertFalse(off.power);
        assertEquals(24,off.temperature);
    }
    @Test public void temperatureStartsCoolingAndRespectsProtocolBounds() {
        ControlState low=new ControlState(18,0,Transcold.FAN,false).next(Command.TEMP_DOWN);
        assertEquals(18,low.temperature);assertTrue(low.power);assertEquals(Transcold.COOL,low.mode);
        assertEquals(30,new ControlState(30,0,Transcold.COOL,true).next(Command.TEMP_UP).temperature);
    }
    @Test public void fanAndModeButtonsKeepIndependentSettings() {
        ControlState initial=new ControlState(25,0,Transcold.COOL,true);
        ControlState high=initial.next(Command.FAN_UP).next(Command.FAN_UP).next(Command.FAN_UP).next(Command.FAN_UP);
        assertEquals(3,high.speed);assertEquals(25,high.temperature);
        assertEquals(0,initial.next(Command.FAN_DOWN).speed);
        assertEquals(Transcold.AUTO,high.next(Command.AUTO).mode);
        assertEquals(Transcold.FAN,high.next(Command.FRESH_AIR).mode);
    }
    @Test public void swingDoesNotInventAPowerStateChange() {
        assertFalse(new ControlState(24,0,Transcold.COOL,false).next(Command.SWING).power);
    }
}
