/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Java adaptation of IRremoteESP8266 ir_Transcold.{h,cpp} (LGPL-2.1-or-later),
 * elected GPL-3.0-or-later under LGPL 2.1 section 3 for this adaptation.
 * Copyright 2020 Chandrashekar Shetty (iamDshetty), crankyoldgit
 * Copyright 2021 siriuslzx. Copyright 2026 Hent IR Remote contributors.
 * See THIRD_PARTY_NOTICES.md. Not verified with Hent parking air conditioners.
 */
package io.github.folkzb.hentremote;

public final class Transcold {
    public static final int FREQUENCY_HZ = 38000;
    public static final int OFF = 0xEF7954;
    public static final int SWING = 0xE76154;
    public static final int COOL = 6, AUTO = 14, FAN = 2;
    private Transcold() {}

    public static int state(int temp, int mode, int speed) {
        if (temp < 18 || temp > 30) throw new IllegalArgumentException("Temperature: 18..30");
        if (mode != COOL && mode != AUTO && mode != FAN) throw new IllegalArgumentException("Mode");
        if (speed < 0 || speed > 3) throw new IllegalArgumentException("Fan: 0..3");
        int nibble = Integer.reverse((~(temp - 17)) & 15) >>> 28;
        int fan = new int[]{mode == AUTO ? 6 : 15, 9, 13, 11}[speed];
        if (mode == FAN) { mode = 12; nibble = 15; }
        return 0xE00054 | (fan << 16) | (mode << 12) | (nibble << 8);
    }

    /** Three MSB-first bytes, each immediately followed by its complement. */
    public static int[] pattern(int code) {
        return pattern(code,new int[]{5944,7563,555,3556,1526,7563,100000});
    }

    public static int[] pattern(int code,int[] timing) {
        if(timing==null||timing.length!=7)throw new IllegalArgumentException("Seven timings required");
        for(int duration:timing)if(duration<=0)throw new IllegalArgumentException("Positive timings required");
        if (code < 0 || code > 0xFFFFFF) throw new IllegalArgumentException("24-bit code required");
        int[] pulses = new int[102];
        int i = 0;
        pulses[i++] = timing[0]; pulses[i++] = timing[1];
        for (int shift = 16; shift >= 0; shift -= 8) {
            int value = (code >>> shift) & 255;
            int pair = (value << 8) | ((~value) & 255);
            for (int bit = 15; bit >= 0; bit--) {
                pulses[i++] = timing[2];
                pulses[i++] = ((pair >>> bit) & 1) == 1 ? timing[3] : timing[4];
            }
        }
        pulses[i++] = timing[2]; pulses[i++] = timing[5];
        pulses[i++] = timing[2]; pulses[i] = timing[6];
        return pulses;
    }
}
