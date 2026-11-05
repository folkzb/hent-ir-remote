// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

import static org.junit.Assert.assertNotNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
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
