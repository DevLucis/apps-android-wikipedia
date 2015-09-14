package org.wikipedia.test.perf;

import org.wikipedia.page.PageActivity;
import org.wikipedia.page.PageFragment;
import org.wikipedia.page.PageLoadCallbacks;
import org.wikipedia.page.PageLoadTests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test performance of page loading. Update the NUM_RUNS for better statistical significance.
 */
@LargeTest
public class PageLoadPerformanceTests extends ActivityInstrumentationTestCase2<PageActivity> {
    private static final int TASK_COMPLETION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
    private static final int NUM_RUNS = 1; //50;
    private final MeasurementController measurement = new MeasurementController();

    public PageLoadPerformanceTests() {
        super(PageActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Launch Activity.
        getActivity();
    }

    public void testLoadPages() throws Throwable {
        loadPageMultipleTimes("Test_page_for_app_testing/Section1");
        loadPageMultipleTimes("A_long_page");
        loadPageMultipleTimes("Barack_Obama"); // much longer than previous pages, has a lead image

        measurement.analyzeAll();
    }

    private void loadPageMultipleTimes(String title) throws Throwable {
        for (int i = 0; i < NUM_RUNS; i++) {
            loadPageUi(title);
        }
    }

    private void loadPageUi(final String title) throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getFragment().setPageLoadCallbacks(newCallbacks(title, latch));

                measurement.start(title);
                PageLoadTests.loadPage(getFragment(), title);
            }
        });
        assertTrue(latch.await(TASK_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    private PageLoadCallbacks newCallbacks(final String title, final CountDownLatch latch) {
        return new PageLoadCallbacks() {
            @Override
            public void onLoadComplete() {
                measurement.stop(title);
                latch.countDown();
            }
        };
    }

    private PageFragment getFragment() {
        return (PageFragment) getActivity().getTopFragment();
    }
}
