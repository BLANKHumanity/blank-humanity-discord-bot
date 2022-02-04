package de.zorro909.blank.BlankDiscordBot.utils;

import java.util.Map;
import java.util.regex.Pattern;

public class NamedFormatter {
    
    private static final Pattern RE = Pattern
	    .compile("\\\\(.)" // Treat any character after a backslash
			       // literally
		    + "|" + "(%\\(([^)]+)\\))" // Look for %(keys) to replace
	    );

    private NamedFormatter() {
    }

    /**
     * Expands format strings containing <code>%(keys)</code>.
     *
     * <p>
     * Examples:
     * </p>
     *
     * <ul>
     * <li><code>NamedFormatter.format("Hello, %(name)!", Map.of("name", "200_success"))</code>
     * → <code>"Hello, 200_success!"</code></li>
     * <li><code>NamedFormatter.format("Hello, \%(name)!", Map.of("name", "200_success"))</code>
     * → <code>"Hello, %(name)!"</code></li>
     * <li><code>NamedFormatter.format("Hello, %(name)!", Map.of("foo", "bar"))</code>
     * → <code>"Hello, %(name)!"</code></li>
     * </ul>
     *
     * @param fmt The format string. Any character in the format string that
     *            follows a backslash is treated literally. Any
     *            <code>%(key)</code> is replaced by its corresponding value in
     *            the <code>values</code> map. If the key does not exist in the
     *            <code>values</code> map, then it is left unsubstituted.
     *
     * @param map Key-value pairs to be used in the substitutions.
     *
     * @return The formatted string.
     */
    public static String namedFormat(String fmt, Map<String, Object> map) {
	return RE
		.matcher(fmt)
		.replaceAll(match -> match.group(1) != null ? match.group(1)
			: map
				.getOrDefault(match.group(3), match.group(2))
				.toString());
    }

}