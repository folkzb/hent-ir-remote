// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;
import org.json.JSONObject;
import org.json.JSONException;

/** Editable, bundled code table. No network or user setup is required. */
final class CandidateProtocol {
    final int frequencyHz;
    final int powerOff, swing;
    private final int[] timings;
    private final int[][][] states = new int[3][13][4];
    private CandidateProtocol(JSONObject root) throws JSONException {
        if (root.getInt("schemaVersion") != 1) throw new IllegalArgumentException("Unsupported catalog version");
        frequencyHz = root.getInt("frequencyHz");
        JSONObject timing = root.getJSONObject("timingsUs");
        String[] keys = {"headerMark","headerSpace","bitMark","oneSpace","zeroSpace","footerSpace","gap"};
        timings = new int[keys.length];
        for (int i=0;i<keys.length;i++) { timings[i]=timing.getInt(keys[i]); if(timings[i]<=0)throw new IllegalArgumentException("Invalid timing"); }
        powerOff = hex(root.getJSONObject("commands").getString("powerOff"));
        swing = hex(root.getJSONObject("commands").getString("swing"));
        String[] modes = {"cool","auto","fan"};
        JSONObject table = root.getJSONObject("states");
        for(int m=0;m<3;m++)for(int t=18;t<=30;t++) {
            org.json.JSONArray row=table.getJSONObject(modes[m]).getJSONArray(Integer.toString(t));
            if(row.length()!=4)throw new IllegalArgumentException("Four fan states required");
            for(int fan=0;fan<4;fan++) { states[m][t-18][fan]=hex(row.getString(fan)); signal(states[m][t-18][fan]); }
        }
        signal(powerOff); signal(swing);
    }
    static CandidateProtocol parse(String input) throws JSONException { return new CandidateProtocol(new JSONObject(input)); }
    private static int hex(String value) {
        if(!value.matches("[0-9a-fA-F]{6}"))throw new IllegalArgumentException("Expected a 24-bit hex code");
        return Integer.parseInt(value,16);
    }
    int state(int temperature,int mode,int speed) {
        if(temperature<18||temperature>30||speed<0||speed>3)throw new IllegalArgumentException("Unsupported state");
        int m;
        if(mode==Transcold.COOL)m=0;else if(mode==Transcold.AUTO)m=1;else if(mode==Transcold.FAN)m=2;else throw new IllegalArgumentException("Unsupported mode");
        return states[m][temperature-18][speed];
    }
    IrSignal signal(int code) { return new IrSignal(frequencyHz,Transcold.pattern(code,timings)); }
}
