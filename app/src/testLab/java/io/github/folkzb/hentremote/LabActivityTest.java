// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

import static org.junit.Assert.assertNotNull;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "en")
public class LabActivityTest {
    @Test public void loadsBatchAndKeepsPositionAfterRecreation() {
        try (var controller = Robolectric.buildActivity(LabActivity.class)) {
            LabActivity app = controller.setup().get();
            View root = app.findViewById(android.R.id.content);
            assertNotNull(find(root, "A01-ON · Coolix"));
            assertNotNull(find(root, "Batch A · 1 of 25"));
            View next = find(root, "Next");
            assertNotNull(next);
            next.performClick();
            assertNotNull(find(root, "A01-OFF · Coolix"));
            app = controller.recreate().get();
            assertNotNull(find(app.findViewById(android.R.id.content), "A01-OFF · Coolix"));
        }
    }

    @Test public void opensExtendedBatchAndSkipsOffSignal() {
        RuntimeEnvironment.getApplication().getSharedPreferences("lab", Context.MODE_PRIVATE)
            .edit().putInt("batch", 1).commit();
        try (var controller = Robolectric.buildActivity(LabActivity.class)) {
            View root = controller.setup().get().findViewById(android.R.id.content);
            assertNotNull(find(root, "B01-ON · Amcor"));
            assertNotNull(find(root, "Batch B · 1 of 47"));
            View skip = find(root, "Skip to next ON or toggle");
            assertNotNull(skip);
            skip.performClick();
            assertNotNull(find(root, "B02-ON · Carrier 64"));
        }
    }

    private static View find(View view, String text) {
        if (view instanceof TextView && text.contentEquals(((TextView) view).getText())) return view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = find(group.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
