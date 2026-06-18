package kami.gg.souppvp.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by DaddyDombo daddydombo@gmail.com on 9/26/2020.
 */
public class DurationFormatter {
    private static final long MINUTE;
    private static final long HOUR;

    static {
        MINUTE = TimeUnit.MINUTES.toMillis(1L);
        HOUR = TimeUnit.HOURS.toMillis(1L);
    }

    public static String getRemaining(final long millis, final boolean milliseconds) {
        return getRemaining(millis, milliseconds, true);
    }

    public static String getRemaining(final long duration, final boolean milliseconds, final boolean trail) {
        if (milliseconds && duration < DurationFormatter.MINUTE) {
            return String.valueOf((trail ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS).get().format(duration * 0.001)) + 's';
        }
        return DurationFormatUtils.formatDuration(duration, ((duration >= DurationFormatter.HOUR) ? "HH:" : "") + "mm:ss");
    }
}
