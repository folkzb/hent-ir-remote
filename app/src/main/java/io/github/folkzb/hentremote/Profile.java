// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.EnumMap;
import java.util.Iterator;

final class Profile {
    String name = "Hent 12V / 24V";
    String source = "";
    final EnumMap<Command, IrSignal> commands = new EnumMap<>(Command.class);

    static Profile parse(String input) throws JSONException {
        JSONObject root = new JSONObject(input);
        if (exactInt(root.get("schemaVersion")) != 1) throw new IllegalArgumentException("schemaVersion must be 1");
        Profile profile = new Profile();
        profile.name = root.getString("name");
        if (profile.name.trim().isEmpty() || profile.name.length() > 100) throw new IllegalArgumentException("Invalid profile name");
        profile.source = root.optString("source", "");
        if (profile.source.length() > 1000) throw new IllegalArgumentException("Source too long");
        JSONObject commands = root.getJSONObject("commands");
        Iterator<String> keys = commands.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Command command = Command.fromId(key);
            JSONObject value = commands.getJSONObject(key);
            JSONArray array = value.getJSONArray("patternUs");
            if (array.length() > 4096) throw new IllegalArgumentException("Too many pulses");
            int[] pulses = new int[array.length()];
            for (int i = 0; i < pulses.length; i++) pulses[i] = exactInt(array.get(i));
            profile.commands.put(command, new IrSignal(exactInt(value.get("frequencyHz")), pulses));
        }
        return profile;
    }
    private static int exactInt(Object value) {
        if (!(value instanceof Number)) throw new IllegalArgumentException("Expected integer");
        double n = ((Number) value).doubleValue();
        if ((Double.isNaN(n) || Double.isInfinite(n)) || n != Math.rint(n) || n < Integer.MIN_VALUE || n > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Expected integer");
        return (int)n;
    }
    String json() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", 1); root.put("name", name); root.put("source", source);
        JSONObject codes = new JSONObject();
        for (Command c : commands.keySet()) {
            IrSignal signal = commands.get(c);
            JSONObject value = new JSONObject();
            value.put("frequencyHz", signal.frequencyHz);
            value.put("patternUs", new JSONArray(signal.patternUs()));
            codes.put(c.id, value);
        }
        root.put("commands", codes);
        return root.toString(2);
    }
}
