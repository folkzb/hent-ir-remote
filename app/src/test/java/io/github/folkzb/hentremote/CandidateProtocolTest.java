package io.github.folkzb.hentremote;
import org.junit.Test;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
public class CandidateProtocolTest {
    private String read(String path) throws Exception {
        try(FileInputStream in=new FileInputStream(path)) {
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
            return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }
    }
    private String fixture() throws Exception { return read("src/main/assets/ir/transcold-candidate.json"); }
    @Test public void bundledCodesMatchAllReferenceStates() throws Exception {
        CandidateProtocol p=CandidateProtocol.parse(fixture());
        for(int mode:new int[]{Transcold.COOL,Transcold.AUTO,Transcold.FAN})
            for(int temp=18;temp<=30;temp++)for(int fan=0;fan<4;fan++)
                assertEquals(Transcold.state(temp,mode,fan),p.state(temp,mode,fan));
        assertEquals(Transcold.OFF,p.powerOff);assertEquals(Transcold.SWING,p.swing);
        assertArrayEquals(Transcold.pattern(0xE96554),p.signal(0xE96554).patternUs());
    }
    @Test public void repositoryEditsActuallyChangeGeneratedSignal() throws Exception {
        JSONObject json=new JSONObject(fixture());json.put("frequencyHz",40000);
        json.getJSONObject("timingsUs").put("headerMark",6000);
        json.getJSONObject("states").getJSONObject("cool").getJSONArray("24").put(0,"E96554");
        json.getJSONObject("commands").put("powerOff","E76154");
        CandidateProtocol p=CandidateProtocol.parse(json.toString());
        assertEquals(0xE96554,p.state(24,Transcold.COOL,0));
        assertEquals(40000,p.signal(p.powerOff).frequencyHz);
        assertEquals(6000,p.signal(p.powerOff).patternUs()[0]);
        assertEquals(0xE76154,p.powerOff);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsMalformedHex() throws Exception {
        JSONObject json=new JSONObject(fixture());json.getJSONObject("commands").put("swing","not-a-code");CandidateProtocol.parse(json.toString());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidCarrier() throws Exception {
        JSONObject json=new JSONObject(fixture());json.put("frequencyHz",0);CandidateProtocol.parse(json.toString());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsNonpositiveTimings() throws Exception {
        JSONObject json=new JSONObject(fixture());json.getJSONObject("timingsUs").put("bitMark",0);CandidateProtocol.parse(json.toString());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsOversizeWaveforms() throws Exception {
        JSONObject json=new JSONObject(fixture());json.getJSONObject("timingsUs").put("gap",2000000);CandidateProtocol.parse(json.toString());
    }
    @Test public void bundledOverridesAreValidWithoutInventedCommands() throws Exception {
        Profile profile=Profile.parse(read("src/main/assets/ir/original-overrides.json"));
        assertTrue(profile.commands.isEmpty());
    }
}
