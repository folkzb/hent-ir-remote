// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

public final class IrSignal {
    public final int frequencyHz;
    private final int[] pattern;
    public IrSignal(int frequencyHz, int[] pattern) {
        if (frequencyHz < 10000 || frequencyHz > 100000) throw new IllegalArgumentException("Carrier: 10000..100000 Hz");
        if (pattern == null || pattern.length < 2 || pattern.length > 4096) throw new IllegalArgumentException("Pattern: 2..4096 pulses");
        long total = 0;
        for (int pulse : pattern) {
            if (pulse <= 0) throw new IllegalArgumentException("Pulse durations must be positive");
            total += pulse;
        }
        if (total >= 2000000) throw new IllegalArgumentException("Pattern must be shorter than 2 seconds");
        this.frequencyHz = frequencyHz;
        this.pattern = pattern.clone();
    }
    public int[] patternUs() { return pattern.clone(); }
}
