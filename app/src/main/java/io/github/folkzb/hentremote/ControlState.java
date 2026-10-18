// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

/** Requested remote state; committed only after a successful transmission. */
final class ControlState {
    final int temperature, speed, mode;
    final boolean power;
    ControlState(int temperature, int speed, int mode, boolean power) {
        if (temperature < 18 || temperature > 30 || speed < 0 || speed > 3)
            throw new IllegalArgumentException("Invalid remote state");
        if (mode != Transcold.COOL && mode != Transcold.AUTO && mode != Transcold.FAN)
            throw new IllegalArgumentException("Invalid mode");
        this.temperature = temperature; this.speed = speed; this.mode = mode; this.power = power;
    }
    ControlState next(Command command) {
        int temp = temperature, fan = speed, nextMode = mode;
        boolean on = power;
        switch (command) {
            case POWER: on = !power; break;
            case TEMP_UP: temp = Math.min(30, temperature + 1); nextMode = Transcold.COOL; on = true; break;
            case TEMP_DOWN: temp = Math.max(18, temperature - 1); nextMode = Transcold.COOL; on = true; break;
            case FAN_UP: fan = Math.min(3, speed + 1); on = true; break;
            case FAN_DOWN: fan = Math.max(0, speed - 1); on = true; break;
            case AUTO: nextMode = Transcold.AUTO; on = true; break;
            case FRESH_AIR: nextMode = Transcold.FAN; on = true; break;
            default: break;
        }
        return new ControlState(temp, fan, nextMode, on);
    }
}
