// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Build;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int BACKGROUND = 0xFF10171E;
    private static final int INK = 0xFFE1E8ED, MUTED = 0xFF93A5B2, LCD = 0xFF192C27, LCD_INK = 0xFFCBF7CB;
    private SharedPreferences preferences;
    private ConsumerIrManager ir;
    private Profile profile = new Profile();
    private CandidateProtocol protocol;
    private ControlState state;
    private final ExecutorService sender = Executors.newSingleThreadExecutor();
    private final Map<Command, Button> buttons = new EnumMap<>(Command.class);
    private TextView temperature, mode, powerLabel, fanLabel, irStatus;
    private final View[] fanBars = new View[3];
    private LinearLayout lcd;
    private TransmissionLight light;
    private boolean busy, emitterAvailable;
    private int transmissionSequence;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        enterFullscreen();
        preferences = getSharedPreferences("remote", MODE_PRIVATE);
        int storedMode = preferences.getInt("mode", Transcold.COOL);
        if (storedMode != Transcold.COOL && storedMode != Transcold.AUTO && storedMode != Transcold.FAN) storedMode = Transcold.COOL;
        state = new ControlState(Math.max(18, Math.min(30, preferences.getInt("temperature", 24))),
            Math.max(0, Math.min(3, preferences.getInt("speed", 0))), storedMode, preferences.getBoolean("power", false));
        // Only bundled repository assets configure signals. Legacy user imports are ignored.
        try { protocol = CandidateProtocol.parse(readAsset("ir/transcold-candidate.json")); }
        catch (Exception ignored) { protocol = null; }
        try { profile = Profile.parse(readAsset("ir/original-overrides.json")); }
        catch (Exception ignored) { profile = new Profile(); }
        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        try { emitterAvailable = ir != null && ir.hasIrEmitter(); }
        catch (RuntimeException ignored) { emitterAvailable = false; }
        buildRemote();
        refresh();
    }
    private void enterFullscreen() {
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(attributes);
        }
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterFullscreen();
    }
    private String readAsset(String path) throws IOException {
        try (InputStream stream = getAssets().open(path)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int count;
            while ((count = stream.read(buffer)) != -1) {
                if (out.size() + count > 1048576) throw new IOException("Catalog too large");
                out.write(buffer, 0, count);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private GradientDrawable surface(int fill, int radius, int border) {
        GradientDrawable drawable = new GradientDrawable(); drawable.setColor(fill); drawable.setCornerRadius(dp(radius));
        if (border != 0) drawable.setStroke(dp(1), border);
        return drawable;
    }
    private TextView label(String text, int size, int color) {
        TextView view = new TextView(this); view.setText(text); view.setTextSize(size); view.setTextColor(color);
        view.setGravity(Gravity.CENTER); view.setIncludeFontPadding(false); return view;
    }
    private void space(LinearLayout parent, int height) { parent.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height), 1f)); }
    private LinearLayout horizontal() { LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); return row; }
    private void buildRemote() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BACKGROUND);
        scroll.setVerticalScrollBarEnabled(false); scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setGravity(Gravity.CENTER_HORIZONTAL);
        body.setPadding(dp(24), dp(12), dp(24), dp(24)); body.setBackgroundColor(BACKGROUND);
        scroll.addView(body, new ScrollView.LayoutParams(-1, -2)); setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            // Keep touch controls clear of physical camera cutouts without reserving hidden system bars.
            int left = 0, top = 0, right = 0, bottom = 0;
            if (Build.VERSION.SDK_INT >= 28 && insets.getDisplayCutout() != null) {
                left = insets.getDisplayCutout().getSafeInsetLeft(); top = insets.getDisplayCutout().getSafeInsetTop();
                right = insets.getDisplayCutout().getSafeInsetRight(); bottom = insets.getDisplayCutout().getSafeInsetBottom();
            }
            view.setPadding(left, top, right, bottom); return insets;
        });
        scroll.requestApplyInsets(); enterFullscreen();
        FrameLayout emitter = new FrameLayout(this);
        View window = new View(this); window.setBackground(surface(0xFF394C56, 4, 0));
        FrameLayout.LayoutParams windowParams = new FrameLayout.LayoutParams(dp(40), dp(6), Gravity.CENTER);
        emitter.addView(window, windowParams); light = new TransmissionLight();
        FrameLayout.LayoutParams lightParams = new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.END | Gravity.CENTER_VERTICAL);
        lightParams.setMarginEnd(dp(8)); emitter.addView(light, lightParams);
        body.addView(emitter, new LinearLayout.LayoutParams(-1, dp(24)));
        space(body, 10);
        LinearLayout header = horizontal();
        TextView brand = label(getString(R.string.brand), 15, INK); brand.setLetterSpacing(.20f); brand.setTypeface(null, Typeface.BOLD); brand.setGravity(Gravity.START);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        irStatus = label("", 12, MUTED); irStatus.setTypeface(Typeface.MONOSPACE); header.addView(irStatus);
        body.addView(header, new LinearLayout.LayoutParams(-1, -2)); space(body, 18);
        lcd = new LinearLayout(this); lcd.setOrientation(LinearLayout.VERTICAL); lcd.setPadding(dp(18), dp(14), dp(18), dp(14));
        lcd.setBackground(surface(LCD, 18, 0xFF355344));
        LinearLayout lcdHeader = horizontal();
        mode = label("", 11, LCD_INK); mode.setTypeface(null, Typeface.BOLD); mode.setGravity(Gravity.START);
        lcdHeader.addView(mode, new LinearLayout.LayoutParams(0, -2, 1));
        powerLabel = label("", 10, LCD_INK); powerLabel.setTypeface(Typeface.MONOSPACE); lcdHeader.addView(powerLabel);
        lcd.addView(lcdHeader);
        LinearLayout reading = horizontal(); reading.setGravity(Gravity.CENTER);
        temperature = label("", 96, LCD_INK); temperature.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        reading.addView(temperature);
        TextView unit = label(getString(R.string.celsius), 23, LCD_INK);
        LinearLayout.LayoutParams unitParams = new LinearLayout.LayoutParams(-2, -2); unitParams.setMarginStart(dp(6)); reading.addView(unit, unitParams);
        lcd.addView(reading, new LinearLayout.LayoutParams(-1, dp(128)));
        LinearLayout fanRow = horizontal();
        fanLabel = label("", 10, LCD_INK); fanLabel.setGravity(Gravity.START); fanRow.addView(fanLabel,new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout bars = horizontal(); bars.setGravity(Gravity.BOTTOM);
        for (int i = 0; i < fanBars.length; i++) {
            View bar = new View(this); bar.setBackground(surface(LCD_INK, 2, 0)); fanBars[i] = bar;
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dp(6), dp(7 + i * 4)); barParams.setMarginStart(dp(4)); bars.addView(bar, barParams);
        }
        fanRow.addView(bars); lcd.addView(fanRow); body.addView(lcd, new LinearLayout.LayoutParams(-1, -2)); space(body, 24);
        LinearLayout topKeys = horizontal(); topKeys.setGravity(Gravity.CENTER);
        addButton(topKeys, Command.LIGHT, getString(R.string.light), 58, 15, false);
        View gap = new View(this); topKeys.addView(gap, new LinearLayout.LayoutParams(0, 1, 1));
        addButton(topKeys, Command.POWER, getString(R.string.power_symbol), 70, 34, true);
        topKeys.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        addButton(topKeys, Command.DISPLAY, getString(R.string.display), 58, 13, false);
        body.addView(topKeys, new LinearLayout.LayoutParams(-1, -2)); space(body, 22);
        TextView tempCaption = label(getString(R.string.temperature_caption), 10, MUTED); tempCaption.setLetterSpacing(.10f); body.addView(tempCaption); space(body, 6);
        LinearLayout temperatureKeys = horizontal();
        addButton(temperatureKeys, Command.TEMP_DOWN, getString(R.string.minus), 88, 34, false);
        TextView tempUnit = label(getString(R.string.celsius), 17, MUTED); temperatureKeys.addView(tempUnit, new LinearLayout.LayoutParams(0, -2, 1));
        addButton(temperatureKeys, Command.TEMP_UP, getString(R.string.plus), 88, 34, false);
        body.addView(temperatureKeys, new LinearLayout.LayoutParams(-1, -2)); space(body, 16);
        row(body, Command.FAN_DOWN, Command.AUTO, Command.FAN_UP); space(body, 12);
        row(body, Command.STRONG, Command.ECO, Command.SLEEP); space(body, 12);
        row(body, Command.SWING, Command.FRESH_AIR); space(body, 22);
        TextView footer = label(getString(R.string.remote_caption), 9, MUTED); footer.setLetterSpacing(.13f); body.addView(footer);
    }
    private void addButton(LinearLayout row, Command command, String title, int width, int textSize, boolean power) {
        Button button = new Button(this); button.setText(title); button.setTextSize(textSize); button.setAllCaps(false);
        button.setTextColor(power ? Color.WHITE : INK); button.setContentDescription(getString(command.label));
        button.setGravity(Gravity.CENTER); button.setPadding(dp(4), 0, dp(4), 0); button.setMinWidth(0); button.setMinimumWidth(0);
        button.setMinHeight(dp(62)); button.setStateListAnimator(null); button.setBackgroundTintList((ColorStateList)null);
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_pressed}, surface(power ? 0xFFA64A44 : 0xFF2A3C48, power ? 36 : 17, 0xFF3B4D59));
        background.addState(new int[]{}, surface(power ? 0xFFCD6159 : 0xFF1E2B36, power ? 36 : 17, power ? 0xFFDF786B : 0xFF2D3C47));
        button.setBackground(background); button.setElevation(dp(2));
        button.setOnClickListener(view -> press(command)); buttons.put(command, button);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width == 0 ? 0 : dp(width), dp(power ? 70 : 62), width == 0 ? 1 : 0);
        params.setMargins(dp(3), dp(3), dp(3), dp(3)); row.addView(button, params);
    }
    private void row(LinearLayout body, Command... commands) {
        LinearLayout row = horizontal();
        for (Command command : commands) addButton(row, command, getString(command.label), 0, 12, false);
        body.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }
    private void refresh() {
        temperature.setText(state.power ? Integer.toString(state.temperature) : getString(R.string.off_temperature));
        powerLabel.setText(state.power ? R.string.on : R.string.off);
        mode.setText(state.mode == Transcold.AUTO ? R.string.mode_auto : state.mode == Transcold.FAN ? R.string.mode_fan : R.string.mode_cool);
        String[] speeds = getResources().getStringArray(R.array.fans);
        fanLabel.setText(getString(R.string.fan_setting, speeds[state.speed]));
        lcd.setAlpha(state.power ? 1f : .66f);
        for (int i = 0; i < fanBars.length; i++) fanBars[i].setAlpha(state.power && (state.speed == 0 || i < state.speed) ? 1f : .17f);
        setIrStatus(emitterAvailable);
        for (Map.Entry<Command, Button> entry : buttons.entrySet()) {
            boolean available = profile.commands.containsKey(entry.getKey()) || (protocol != null && entry.getKey().hasCandidate());
            entry.getValue().setEnabled(emitterAvailable && available && !busy);
            entry.getValue().setAlpha(available ? 1f : .35f);
        }
        buttons.get(Command.POWER).setSelected(state.power);
    }
    private void setIrStatus(boolean ok) {
        irStatus.setText(ok ? R.string.ir_ok : R.string.ir_not_ok);
        irStatus.setTextColor(ok ? 0xFF8BCDAA : 0xFFEC9A94);
    }
    private void press(Command command) {
        if (busy || !emitterAvailable) return;
        ControlState next = state.next(command);
        IrSignal signal = profile.commands.get(command);
        if (signal == null) {
            if (protocol == null || !command.hasCandidate()) return;
            int code = command == Command.POWER && !next.power ? protocol.powerOff :
                command == Command.SWING ? protocol.swing : protocol.state(next.temperature, next.mode, next.speed);
            signal = protocol.signal(code);
        }
        try {
            ConsumerIrManager.CarrierFrequencyRange[] ranges = ir.getCarrierFrequencies();
            if (ranges != null) {
                boolean supported = false;
                for (ConsumerIrManager.CarrierFrequencyRange range : ranges)
                    if (signal.frequencyHz >= range.getMinFrequency() && signal.frequencyHz <= range.getMaxFrequency()) supported = true;
                if (!supported) { setIrStatus(false); return; }
            }
        } catch (RuntimeException exception) { setIrStatus(false); return; }
        final IrSignal sending = signal;
        final int sequence = ++transmissionSequence;
        final long started = SystemClock.uptimeMillis();
        busy = true; refresh(); light.setLit(true);
        sender.execute(() -> {
            boolean success;
            try { ir.transmit(sending.frequencyHz, sending.patternUs()); success = true; }
            catch (RuntimeException exception) { success = false; }
            final boolean sent = success;
            runOnUiThread(() -> {
                if (isDestroyed()) return;
                if (sent) {
                    state = next;
                    preferences.edit().putInt("temperature", state.temperature).putInt("speed", state.speed)
                        .putInt("mode", state.mode).putBoolean("power", state.power).apply();
                }
                busy = false; refresh(); if (!sent) setIrStatus(false);
                light.postDelayed(() -> { if (sequence == transmissionSequence && !isDestroyed()) light.setLit(false); },
                    Math.max(0, 180 - (SystemClock.uptimeMillis() - started)));
            });
        });
    }
    @Override protected void onDestroy() { sender.shutdown(); super.onDestroy(); }

    private final class TransmissionLight extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean lit;
        TransmissionLight() { super(MainActivity.this); setLit(false); setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES); }
        void setLit(boolean value) { lit = value; setContentDescription(getString(value ? R.string.transmitter_active : R.string.transmitter_idle)); invalidate(); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas); float cx = getWidth() / 2f, cy = getHeight() / 2f;
            if (lit) { paint.setColor(0x35FF5149); canvas.drawCircle(cx, cy, dp(9), paint); }
            paint.setColor(lit ? 0xFFFF5149 : 0xFF4D5D65); canvas.drawCircle(cx, cy, dp(4), paint);
            paint.setColor(lit ? 0xFFFFC7B9 : 0xFF6C7E87); canvas.drawCircle(cx - dp(1), cy - dp(1), dp(1), paint);
        }
    }
}
