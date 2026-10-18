// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public final class MainActivity extends Activity {
    private static final int IMPORT = 10, EXPORT = 11, REPORT = 12;
    private static final int INK = Color.rgb(16,28,37), PANEL = Color.rgb(27,43,55);
    private static final int MINT = Color.rgb(115,228,210), WHITE = Color.rgb(238,246,248);
    private static final int MUTED = Color.rgb(169,189,200);
    private SharedPreferences preferences;
    private ConsumerIrManager ir;
    private Profile profile = new Profile();
    private CandidateProtocol protocol;
    private final ExecutorService sender = Executors.newSingleThreadExecutor();
    private final List<Button> commandButtons = new ArrayList<>();
    private LinearLayout page;
    private TextView state, status, mapping;
    private int temperature = 24, speed = 0, mode = Transcold.COOL;
    private boolean experimental, busy;
    private String lastCommand = "", lastError = "";
    private int lastFrequency;
    private long lastSentAt;

    @Override protected void attachBaseContext(Context base) {
        String language = base.getSharedPreferences("remote", MODE_PRIVATE).getString("language", "system");
        if (!"system".equals(language)) {
            Configuration config = new Configuration(base.getResources().getConfiguration());
            config.setLocale(Locale.forLanguageTag(language));
            base = base.createConfigurationContext(config);
        }
        super.attachBaseContext(base);
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        preferences = getSharedPreferences("remote", MODE_PRIVATE);
        temperature = preferences.getInt("temperature", 24);
        speed = preferences.getInt("speed", 0);
        mode = preferences.getInt("mode", Transcold.COOL);
        experimental = preferences.getBoolean("experimental", true);
        try { protocol = CandidateProtocol.parse(readAsset("ir/transcold-candidate.json")); }
        catch (Exception e) { experimental = false; lastError = e.getClass().getSimpleName(); }
        String raw = preferences.getString("profile", "");
        try { profile = Profile.parse(raw.isEmpty() ? readAsset("ir/original-overrides.json") : raw); }
        catch (Exception ignored) { /* Keep empty profile if invalid. */ }
        ir = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
        buildUi();
        if (protocol == null) status.setText(R.string.file_error);
        if (saved != null) {
            lastCommand = saved.getString("lastCommand", "");
            lastSentAt = saved.getLong("lastSentAt", 0);
            if (lastSentAt > 0) status.setText(getString(R.string.sent, lastCommand));
        }
    }
    private String readAsset(String path) throws IOException {
        try (InputStream stream=getAssets().open(path)) {
            ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buffer=new byte[8192]; int count;
            while((count=stream.read(buffer))!=-1) {
                if(out.size()+count>1048576)throw new IOException("Catalog too large");
                out.write(buffer,0,count);
            }
            return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }
    }
    private boolean hasEmitter() { try { return ir != null && ir.hasIrEmitter(); } catch (RuntimeException e) { return false; } }
    private static boolean supports(ConsumerIrManager.CarrierFrequencyRange[] ranges, int frequency) {
        for (ConsumerIrManager.CarrierFrequencyRange r : ranges)
            if (frequency >= r.getMinFrequency() && frequency <= r.getMaxFrequency()) return true;
        return false;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private GradientDrawable rounded(int color) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(18)); return d;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(dp(4), dp(8), dp(4), dp(8)); return t;
    }
    private void addText(int resource, int size, int color) { page.addView(text(getString(resource), size, color)); }
    private Button button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setTextSize(13); b.setAllCaps(false);
        b.setTextColor(WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PANEL));
        b.setMinHeight(dp(64)); b.setPadding(dp(5), dp(6), dp(5), dp(6)); b.setOnClickListener(v -> action.run()); return b;
    }
    private void buildUi() {
        commandButtons.clear();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(INK);
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(20), dp(14), dp(20), dp(24));
        scroll.addView(page); setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            // Includes status/navigation bars on Android 15 edge-to-edge.
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom()); return insets;
        });
        scroll.requestApplyInsets();
        TextView eyebrow = text(getString(R.string.eyebrow), 12, MINT); eyebrow.setLetterSpacing(.12f); page.addView(eyebrow);
        TextView title = text(getString(R.string.app_name), 30, WHITE); title.setTypeface(null, Typeface.BOLD); page.addView(title);
        addText(R.string.subtitle, 14, MUTED);
        TextView hardware = text(getString(hasEmitter() ? R.string.ir_available : R.string.no_ir), 13, hasEmitter() ? MINT : Color.rgb(255,191,120));
        page.addView(hardware);
        TextView note = text(getString(R.string.experimental_warning), 14, Color.rgb(255,207,139));
        note.setBackground(rounded(PANEL)); note.setPadding(dp(14), dp(14), dp(14), dp(14)); page.addView(note);
        Switch candidate = new Switch(this); candidate.setText(R.string.experimental_profile); candidate.setTextColor(WHITE);
        candidate.setPadding(0,dp(16),0,dp(12)); candidate.setChecked(experimental); page.addView(candidate);
        candidate.setEnabled(protocol != null);
        candidate.setOnCheckedChangeListener((v, checked) -> {
            if (checked) {
                new AlertDialog.Builder(this).setTitle(R.string.experimental_profile).setMessage(R.string.candidate_explanation)
                    .setPositiveButton(R.string.enable, (d,w) -> { experimental = true; persist(); refresh(); })
                    .setNegativeButton(android.R.string.cancel, (d,w) -> candidate.setChecked(false))
                    .setOnCancelListener(d -> candidate.setChecked(false)).show();
            } else { experimental = false; persist(); refresh(); }
        });
        mapping = text("", 13, MUTED); page.addView(mapping);
        state = text("", 25, MINT); state.setGravity(Gravity.CENTER); state.setBackground(rounded(PANEL)); page.addView(state);
        addText(R.string.local_state, 12, MUTED);
        row(Command.LIGHT, Command.POWER);
        row(Command.TEMP_UP);
        row(Command.FAN_DOWN, Command.AUTO, Command.FAN_UP);
        row(Command.TEMP_DOWN);
        row(Command.STRONG, Command.ECO, Command.SLEEP);
        row(Command.SWING, Command.FRESH_AIR, Command.DISPLAY);
        status = text(getString(R.string.ready), 14, MUTED); status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); page.addView(status);
        addText(R.string.settings, 20, WHITE);
        page.addView(button(getString(R.string.save_settings), () -> { persist(); toast(R.string.saved); }));
        page.addView(button(getString(R.string.import_profile), () -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.setType("*/*"); intent.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(intent, IMPORT);
        }));
        page.addView(button(getString(R.string.export_profile), () -> exportDocument(EXPORT, "hent-ir-profile.json")));
        page.addView(button(getString(R.string.test_result), this::recordResult));
        page.addView(button(getString(R.string.export_report), () -> exportDocument(REPORT, "hent-ir-report.json")));
        page.addView(button(getString(R.string.language), this::chooseLanguage));
        page.addView(button(getString(R.string.help), this::showHelp));
        refresh();
    }
    private void row(Command... commands) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER);
        for (Command command : commands) {
            Button b = button(getString(command.label), () -> press(command)); b.setTag(command);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1); params.setMargins(dp(3),dp(3),dp(3),dp(3));
            row.addView(b,params); commandButtons.add(b);
        }
        page.addView(row);
    }
    private void refresh() {
        String[] fans = getResources().getStringArray(R.array.fans);
        state.setText(getString(R.string.state, temperature, fans[speed]));
        int available = 0;
        for (Command command : Command.values())
            if(profile.commands.containsKey(command) || (experimental && protocol != null && command.hasCandidate())) available++;
        mapping.setText(getString(R.string.mapping_count, profile.name, available, Command.values().length, profile.commands.size()));
        for (Button b : commandButtons) {
            Command c = (Command)b.getTag();
            boolean imported = profile.commands.containsKey(c), known = experimental && protocol != null && c.hasCandidate();
            b.setText(getString(R.string.button_status,getString(c.label),getString(imported ? R.string.imported : known ? R.string.candidate : R.string.unmapped)));
            b.setAlpha(imported || known ? 1f : .60f); b.setEnabled(!busy);
        }
    }
    private void press(Command c) {
        if (busy) return;
        IrSignal raw = profile.commands.get(c);
        if (raw != null) { transmit(c, raw, temperature, speed, mode); return; }
        if (!experimental || protocol == null || !c.hasCandidate()) {
            new AlertDialog.Builder(this).setTitle(c.label).setMessage(R.string.no_code).setPositiveButton(android.R.string.ok,null).show(); return;
        }
        if (c == Command.POWER) {
            new AlertDialog.Builder(this).setTitle(R.string.power_choice).setMessage(R.string.local_state)
                .setPositiveButton(R.string.on, (d,w) -> transmit(c, protocol.signal(protocol.state(temperature,mode,speed)),temperature,speed,mode))
                .setNegativeButton(R.string.off, (d,w) -> transmit(c,protocol.signal(protocol.powerOff),temperature,speed,mode)).show(); return;
        }
        int temp = temperature, fan = speed, nextMode = mode;
        switch (c) {
            case TEMP_UP: temp = Math.min(30,temp+1); break;
            case TEMP_DOWN: temp = Math.max(18,temp-1); break;
            case FAN_UP: fan = Math.min(3,fan+1); break;
            case FAN_DOWN: fan = Math.max(0,fan-1); break;
            case AUTO: nextMode = Transcold.AUTO; break;
            case FRESH_AIR: nextMode = Transcold.FAN; break;
            default: break;
        }
        // Temperature adjustments select cooling after fan/automatic mode.
        if (c == Command.TEMP_UP || c == Command.TEMP_DOWN) nextMode = Transcold.COOL;
        int code = c == Command.SWING ? protocol.swing : protocol.state(temp,nextMode,fan);
        transmit(c,protocol.signal(code),temp,fan,nextMode);
    }
    private void transmit(Command command, IrSignal signal, int temp, int fan, int nextMode) {
        if (busy) return;
        if (!hasEmitter()) { toast(R.string.no_ir); return; }
        final ConsumerIrManager.CarrierFrequencyRange[] ranges;
        try { ranges = ir.getCarrierFrequencies(); }
        catch (RuntimeException e) { showError(e); return; }
        if (ranges != null && !supports(ranges,signal.frequencyHz)) { toast(R.string.unsupported_frequency); return; }
        busy = true; refresh(); status.setText(R.string.sending);
        sender.execute(() -> {
            try {
                ir.transmit(signal.frequencyHz,signal.patternUs());
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    temperature = temp; speed = fan; mode = nextMode;
                    lastCommand = command.id; lastFrequency = signal.frequencyHz; lastSentAt = System.currentTimeMillis(); lastError = "";
                    persist(); busy = false; refresh(); status.setText(getString(R.string.sent,getString(command.label)));
                });
            } catch (RuntimeException e) {
                runOnUiThread(() -> { if (!isDestroyed()) { busy = false; refresh(); showError(e); } });
            }
        });
    }
    private void showError(Exception e) { lastError = e.getClass().getSimpleName(); status.setText(getString(R.string.send_error,lastError)); }
    private void toast(int resource) { Toast.makeText(this,resource,Toast.LENGTH_SHORT).show(); }
    private void persist() {
        preferences.edit().putInt("temperature",temperature).putInt("speed",speed).putInt("mode",mode)
            .putBoolean("experimental",experimental).apply();
    }
    private void exportDocument(int request, String name) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT); intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE); intent.putExtra(Intent.EXTRA_TITLE,name); startActivityForResult(intent,request);
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if (result != RESULT_OK || data == null || data.getData() == null) return;
        try {
            if (request == IMPORT) {
                String input;
                try (InputStream stream = getContentResolver().openInputStream(data.getData())) {
                    if (stream == null) throw new IOException("Unable to open document");
                    ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int n;
                    while ((n = stream.read(buffer)) != -1) {
                        if (output.size()+n > 1048576) throw new IOException("Profile exceeds 1 MiB");
                        output.write(buffer,0,n);
                    }
                    input = new String(output.toByteArray(),StandardCharsets.UTF_8);
                }
                final Profile candidate = Profile.parse(input);
                new AlertDialog.Builder(this).setTitle(R.string.import_profile)
                    .setMessage(getString(R.string.confirm_import,candidate.name,candidate.commands.size()))
                    .setPositiveButton(R.string.replace,(d,w) -> {
                        try { preferences.edit().putString("profile",candidate.json()).putString("test_result","untested").apply(); profile = candidate; refresh(); toast(R.string.imported_ok); }
                        catch (Exception e) { status.setText(R.string.file_error); }
                    }).setNegativeButton(android.R.string.cancel,null).show();
            } else if (request == EXPORT || request == REPORT) {
                String content = request == EXPORT ? profile.json() : report();
                try (OutputStream stream = getContentResolver().openOutputStream(data.getData(),"wt")) {
                    if (stream == null) throw new IOException("Unable to write document");
                    stream.write(content.getBytes(StandardCharsets.UTF_8));
                }
                toast(R.string.exported);
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle(R.string.file_error).setMessage(getString(R.string.profile_format_help))
                .setPositiveButton(android.R.string.ok,null).show();
        }
    }
    private String report() throws Exception {
        JSONObject report = new JSONObject(); report.put("appVersion","0.1.1"); report.put("protocolCandidate",experimental ? "TRANSCOLD" : "none");
        report.put("profile",new JSONObject(profile.json())); report.put("hasIrEmitter",hasEmitter());
        report.put("lastCommand",lastCommand); report.put("lastFrequencyHz",lastFrequency); report.put("lastSentAt",lastSentAt);
        report.put("lastError",lastError); report.put("userTestResult",preferences.getString("test_result","untested"));
        return report.toString(2);
    }
    private void recordResult() {
        String[] labels = getResources().getStringArray(R.array.results);
        String[] values = {"working","partial","not_working","untested"};
        new AlertDialog.Builder(this).setTitle(R.string.test_result).setItems(labels,(d,which) -> {
            preferences.edit().putString("test_result",values[which]).apply(); toast(R.string.saved);
        }).show();
    }
    private void chooseLanguage() {
        String[] labels = {getString(R.string.system_language),"Português (Brasil)","English"};
        String[] values = {"system","pt-BR","en"};
        new AlertDialog.Builder(this).setTitle(R.string.language).setItems(labels,(d,which) -> {
            preferences.edit().putString("language",values[which]).apply(); recreate();
        }).show();
    }
    private void showHelp() {
        new AlertDialog.Builder(this).setTitle(R.string.help).setMessage(R.string.help_text)
            .setPositiveButton(android.R.string.ok,null).show();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putString("lastCommand",lastCommand); out.putLong("lastSentAt",lastSentAt);
    }
    @Override protected void onDestroy() { sender.shutdown(); super.onDestroy(); }
}
