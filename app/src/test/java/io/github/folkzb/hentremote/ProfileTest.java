package io.github.folkzb.hentremote;
import org.junit.Test;
import static org.junit.Assert.*;
public class ProfileTest {
    private String profile(String command) { return "{\"schemaVersion\":1,\"name\":\"Test\",\"commands\":{"+command+"}}"; }
    @Test public void roundTripAllOriginalButtons() throws Exception {
        Profile p=new Profile();
        for(Command c:Command.values()) p.commands.put(c,new IrSignal(38000,new int[]{9000,4500,560,560}));
        Profile copy=Profile.parse(p.json()); assertEquals(13,copy.commands.size());
        for(Command c:Command.values()) assertArrayEquals(p.commands.get(c).patternUs(),copy.commands.get(c).patternUs());
    }
    @Test public void allowsEmptyMappingWithoutInventingCodes() throws Exception { assertTrue(Profile.parse(profile("")).commands.isEmpty()); }
    @Test(expected=IllegalArgumentException.class) public void rejectsFractionalPulse() throws Exception {
        Profile.parse(profile("\"light\":{\"frequencyHz\":38000,\"patternUs\":[560.5,560]}"));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnknownButton() throws Exception {
        Profile.parse(profile("\"unknown\":{\"frequencyHz\":38000,\"patternUs\":[560,560]}"));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsStringCarrier() throws Exception {
        Profile.parse(profile("\"light\":{\"frequencyHz\":\"38000\",\"patternUs\":[560,560]}"));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsEmptyName() throws Exception {
        Profile.parse("{\"schemaVersion\":1,\"name\":\" \",\"commands\":{}}");
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsFractionalVersion() throws Exception {
        Profile.parse("{\"schemaVersion\":1.5,\"name\":\"Test\",\"commands\":{}}");
    }
}
