package kami.gg.souppvp.guild;

import java.util.regex.Pattern;

public final class GuildNameValidator {

    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 16;
    private static final Pattern ALLOWED = Pattern.compile("^[A-Za-z0-9_]+$");

    private GuildNameValidator() {}

    public enum Result { OK, TOO_SHORT, TOO_LONG, INVALID_CHARS, BLOCKED, TAKEN }

    public static Result validateFormat(String name) {
        if (name == null) return Result.INVALID_CHARS;
        if (name.length() < MIN_LENGTH) return Result.TOO_SHORT;
        if (name.length() > MAX_LENGTH) return Result.TOO_LONG;
        if (!ALLOWED.matcher(name).matches()) return Result.INVALID_CHARS;
        return Result.OK;
    }

    public static String describe(Result r) {
        switch (r) {
            case TOO_SHORT:     return "Guild name must be at least " + MIN_LENGTH + " characters.";
            case TOO_LONG:      return "Guild name must be at most "  + MAX_LENGTH + " characters.";
            case INVALID_CHARS: return "Guild names may only contain letters, numbers and underscores.";
            case BLOCKED:       return "That guild name is not allowed.";
            case TAKEN:         return "A guild with that name already exists.";
            default:            return "OK";
        }
    }
}