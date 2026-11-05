// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.hardware.ConsumerIrManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64InputStream;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class LabActivity extends Activity {
    private record Trial(String id, String family, String action, IrSignal signal) {}
    private final List<Trial> trials = new ArrayList<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private SharedPreferences prefs;
    private ConsumerIrManager ir;
    private TextView status, position, title, detail, outcome;
    private Button prev, next, nextPower, send, no, beep, changed, copy;
    private Spinner batches;
    private static final String[] BATCHES = {"A", "B", "C", "D"};
    private String batch;
    private int index;
    private boolean busy, sent;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        prefs = getSharedPreferences("lab", MODE_PRIVATE);

        try {
            ConsumerIrManager manager = getSystemService(ConsumerIrManager.class);
            if (manager != null && manager.hasIrEmitter()) ir = manager;
        } catch (RuntimeException ignored) { ir = null; }

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(16), dp(20), dp(20));
        scroll.addView(panel);
        setContentView(scroll);
        if (Build.VERSION.SDK_INT >= 30) {
            scroll.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets i = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(i.left, i.top, i.right, i.bottom);
                return insets;
            });
            scroll.requestApplyInsets();
        }
        line(panel, getString(R.string.lab_name), 28);
        status = line(panel, getString(ir != null ? R.string.ir_ok : R.string.ir_not_ok), 15);
        line(panel, getString(R.string.lab_hint), 16);
        batches = new Spinner(this);
        String[] batchLabels = new String[BATCHES.length];
        for (int i = 0; i < BATCHES.length; i++)
            batchLabels[i] = getString(R.string.lab_batch, BATCHES[i]);
        ArrayAdapter<String> options = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, batchLabels);
        options.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batches.setAdapter(options);
        batches.setContentDescription(getString(R.string.lab_choose_batch));
        panel.addView(batches);
        position = line(panel, "", 14);
        title = line(panel, "", 24);
        detail = line(panel, "", 17);
        send = button(panel, R.string.lab_send, this::transmit);
        outcome = line(panel, "", 15);
        no = button(panel, R.string.lab_none, () -> result("none"));
        beep = button(panel, R.string.lab_beep, () -> result("beep"));
        changed = button(panel, R.string.lab_changed, () -> result("changed"));
        LinearLayout nav = new LinearLayout(this);
        panel.addView(nav);
        prev = button(nav, R.string.lab_prev, () -> move(-1));
        next = button(nav, R.string.lab_next, () -> move(1));
        nextPower = button(panel, R.string.lab_next_power, () -> {
            int target = nextPowerIndex();
            if (target < trials.size()) move(target - index);
        });
        copy = button(panel, R.string.lab_copy, this::copyResults);
        int selected = Math.max(0, Math.min(BATCHES.length - 1, prefs.getInt("batch", 0)));
        selectBatch(selected);
        batches.setSelection(selected);
        batches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int item, long id) {
                if (!busy && !BATCHES[item].equals(batch)) selectBatch(item);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void selectBatch(int item) {
        batch = BATCHES[item];
        prefs.edit().putInt("batch", item).apply();
        trials.clear();
        sent = false;
        try { loadBatch(); } catch (IOException | JSONException | IllegalArgumentException e) { trials.clear(); }
        String key = batch.equals("A") ? "index" : "index_" + batch;
        index = Math.max(0, Math.min(trials.size() - 1, prefs.getInt(key, 0)));
        showSavedResult();
        refresh();
    }

    private InputStream openBatch() throws IOException {
        if (batch.equals("A")) return getAssets().open("ir/batch-a.json");
        InputStream encoded = getAssets().open("ir/batch-" + batch.toLowerCase(java.util.Locale.ROOT) + ".json.gz.b64");
        Base64InputStream decoded = new Base64InputStream(encoded, Base64.DEFAULT);
        try { return new GZIPInputStream(decoded); }
        catch (IOException e) { decoded.close(); throw e; }
    }

    private void loadBatch() throws IOException, JSONException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream in = openBatch()) {
            byte[] buffer = new byte[4096];
            for (int n; (n = in.read(buffer)) != -1; ) bytes.write(buffer, 0, n);
        }
        JSONArray array = new JSONArray(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String id = obj.getString("id"), action = obj.getString("action");
            if (!ids.add(id) || !(action.equals("ON") || action.equals("OFF") || action.equals("TOGGLE")))
                throw new IllegalArgumentException("Invalid trial: " + id);
            JSONArray durations = obj.getJSONArray("us");
            int[] pattern = new int[durations.length()];
            for (int j = 0; j < pattern.length; j++) pattern[j] = durations.getInt(j);
            trials.add(new Trial(id, obj.getString("family"), action, new IrSignal(obj.getInt("hz"), pattern)));
        }
    }

    private void refresh() {
        boolean available = !trials.isEmpty();
        batches.setEnabled(!busy);
        nextPower.setEnabled(!busy && nextPowerIndex() < trials.size());
        send.setEnabled(available && ir != null && !busy);
        prev.setEnabled(!busy && index > 0);
        next.setEnabled(!busy && index + 1 < trials.size());
        no.setEnabled(sent && !busy); beep.setEnabled(sent && !busy); changed.setEnabled(sent && !busy);
        copy.setEnabled(available && !busy);
        if (!available) { title.setText(R.string.lab_bad_batch); position.setText(""); detail.setText(""); return; }
        Trial t = trials.get(index);
        position.setText(getString(R.string.lab_position, batch, index + 1, trials.size()));
        title.setText(t.id() + " · " + t.family());
        int action = switch (t.action()) {
            case "ON" -> R.string.lab_on;
            case "OFF" -> R.string.lab_off;
            default -> R.string.lab_toggle;
        };
        detail.setText(getString(R.string.lab_details, getString(action), t.signal().frequencyHz));
    }

    private int nextPowerIndex() {
        int target = index + 1;
        while (target < trials.size() && trials.get(target).action().equals("OFF")) target++;
        return target;
    }

    private void move(int delta) {
        if (busy || index + delta < 0 || index + delta >= trials.size()) return;
        index += delta;
        sent = false;
        prefs.edit().putInt(batch.equals("A") ? "index" : "index_" + batch, index).apply();
        showSavedResult();
        refresh();
    }

    private void showSavedResult() {
        if (!trials.isEmpty()) outcome.setText(resultLabel(prefs.getString(trials.get(index).id(), "")));
    }

    private CharSequence resultLabel(String result) {
        return switch (result) {
            case "none" -> getString(R.string.lab_none);
            case "beep" -> getString(R.string.lab_beep);
            case "changed" -> getString(R.string.lab_changed);
            default -> "";
        };
    }

    private void transmit() {
        if (busy || ir == null || trials.isEmpty()) return;
        Trial t = trials.get(index);
        sent = false; busy = true;
        status.setText(R.string.lab_sending);
        outcome.setText("");
        refresh();
        worker.execute(() -> {
            int message = R.string.lab_sent;
            try {
                ConsumerIrManager.CarrierFrequencyRange[] ranges = ir.getCarrierFrequencies();
                boolean supported = ranges == null;
                if (ranges != null)
                    for (ConsumerIrManager.CarrierFrequencyRange range : ranges)
                        if (t.signal().frequencyHz >= range.getMinFrequency()
                            && t.signal().frequencyHz <= range.getMaxFrequency()) supported = true;
                if (supported) ir.transmit(t.signal().frequencyHz, t.signal().patternUs());
                else message = R.string.lab_frequency;
            } catch (RuntimeException e) { message = R.string.lab_error; }
            final int result = message;
            runOnUiThread(() -> {
                if (isDestroyed()) return;
                busy = false;
                sent = result == R.string.lab_sent;
                status.setText(R.string.ir_ok);
                outcome.setText(result);
                refresh();
            });
        });
    }

    private void result(String value) {
        if (!sent || busy) return;
        prefs.edit().putString(trials.get(index).id(), value).apply();
        outcome.setText(resultLabel(value));
        sent = false;
        refresh();
    }

    private void copyResults() {
        StringBuilder report = new StringBuilder("Hent IR Lab · ").append(batch).append(" · ").append(Build.MODEL).append('\n');
        int count = 0;
        for (Trial t : trials) {
            String value = prefs.getString(t.id(), "");
            if (!value.isEmpty()) {
                report.append(t.id()).append(" · ").append(t.family()).append(" · ").append(resultLabel(value)).append('\n');
                count++;
            }
        }
        if (count == 0) { Toast.makeText(this, R.string.lab_empty, Toast.LENGTH_SHORT).show(); return; }
        ClipboardManager clipboard = getSystemService(ClipboardManager.class);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.lab_name), report.toString()));
            Toast.makeText(this, R.string.lab_copied, Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onDestroy() { worker.shutdown(); super.onDestroy(); }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    private TextView line(LinearLayout parent, String text, int size) {
        TextView label = new TextView(this);
        label.setText(text); label.setTextSize(size); label.setPadding(0, dp(6), 0, dp(6));
        parent.addView(label);
        return label;
    }

    private Button button(LinearLayout parent, int label, Runnable action) {
        Button button = new Button(this);
        button.setText(label); button.setAllCaps(false);
        button.setOnClickListener(v -> action.run());
        parent.addView(button, parent.getOrientation() == LinearLayout.VERTICAL
            ? new LinearLayout.LayoutParams(-1, -2) : new LinearLayout.LayoutParams(0, -2, 1));
        return button;
    }
}
