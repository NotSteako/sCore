package kami.gg.souppvp.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by DaddyDombo daddydombo@gmail.com on 9/26/2020.
 */
public final class DateTimeFormats {
    private static final AtomicBoolean loaded;
    public static FastDateFormat DAY_MTH_HR_MIN_SECS;
    public static FastDateFormat DAY_MTH_YR_HR_MIN_AMPM;
    public static FastDateFormat DAY_MTH_HR_MIN_AMPM;
    public static FastDateFormat HR_MIN_AMPM;
    public static FastDateFormat HR_MIN_AMPM_TIMEZONE;
    public static FastDateFormat HR_MIN;
    public static FastDateFormat KOTH_FORMAT;
    public static final ThreadLocal<DecimalFormat> REMAINING_SECONDS;
    public static final ThreadLocal<DecimalFormat> REMAINING_SECONDS_TRAILING;

    static {
        loaded = new AtomicBoolean(false);
        REMAINING_SECONDS = ThreadLocal.withInitial(() -> new DecimalFormat("0.#"));
        REMAINING_SECONDS_TRAILING = ThreadLocal.withInitial(() -> new DecimalFormat("0.0"));
    }

    public static void reload(final TimeZone timeZone) throws IllegalStateException {
        Preconditions.checkArgument(!DateTimeFormats.loaded.getAndSet(true), "Already loaded");
        DateTimeFormats.DAY_MTH_HR_MIN_SECS = FastDateFormat.getInstance("dd/MM HH:mm:ss", timeZone, Locale.ENGLISH);
        DateTimeFormats.DAY_MTH_YR_HR_MIN_AMPM = FastDateFormat.getInstance("dd/MM/yy hh:mma", timeZone, Locale.ENGLISH);
        DateTimeFormats.DAY_MTH_HR_MIN_AMPM = FastDateFormat.getInstance("dd/MM hh:mma", timeZone, Locale.ENGLISH);
        DateTimeFormats.HR_MIN_AMPM = FastDateFormat.getInstance("hh:mma", timeZone, Locale.ENGLISH);
        DateTimeFormats.HR_MIN_AMPM_TIMEZONE = FastDateFormat.getInstance("hh:mma z", timeZone, Locale.ENGLISH);
        DateTimeFormats.HR_MIN = FastDateFormat.getInstance("hh:mm", timeZone, Locale.ENGLISH);
        DateTimeFormats.KOTH_FORMAT = FastDateFormat.getInstance("m:ss", timeZone, Locale.ENGLISH);
    }
}
