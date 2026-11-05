// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.folkzb.hentremote;

import static org.junit.Assert.assertNotNull;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28, 34, 35})
public class MainActivityTest {
    @Test public void opensAndResumesRemote() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class)) {
            MainActivity activity = controller.setup().visible().get();
            activity.onWindowFocusChanged(true);
            View content = activity.findViewById(android.R.id.content);
            assertNotNull(content);
            controller.pause().stop().restart().start().resume();
            activity.onWindowFocusChanged(true);
        }
    }
}
