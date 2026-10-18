// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

public enum Command {
    LIGHT("light", R.string.light), POWER("power", R.string.power),
    TEMP_UP("temp_up", R.string.temp_up), FAN_DOWN("fan_down", R.string.fan_down),
    AUTO("auto", R.string.auto), FAN_UP("fan_up", R.string.fan_up), TEMP_DOWN("temp_down", R.string.temp_down),
    STRONG("strong", R.string.strong), ECO("eco", R.string.eco), SLEEP("sleep", R.string.sleep),
    SWING("swing", R.string.swing), FRESH_AIR("fresh_air", R.string.fresh_air), DISPLAY("display", R.string.display);
    public final String id;
    public final int label;
    Command(String id, int label) { this.id = id; this.label = label; }
    public static Command fromId(String id) {
        for (Command command : values()) if (command.id.equals(id)) return command;
        throw new IllegalArgumentException("Unknown command: " + id);
    }
    public boolean hasCandidate() {
        return this != LIGHT && this != STRONG && this != ECO && this != SLEEP && this != DISPLAY;
    }
}
