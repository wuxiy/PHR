/*********************************************************************************
 * Copyright (c) 2019 中电健康云科技有限公司
 * 版本      DATE             BY             REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0    2020/1/16          wuxi           
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String COLON = Messages.getString("StringUtil.colon");
    public static final String COLON_AND_SPACE = Messages.getString("StringUtil.colon_space");
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final char[] HEX_DIGIT = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private StringUtil() {
    }

    public static String getTruncatedString(String name, int limit, Suffix suffix) {
        if (name != null && name.length() > limit) {
            int sLength = suffix.getLength();
            int end = limit - sLength;
            if (end > 0 && end + sLength < name.length()) {
                return name.substring(0, end).concat(suffix.getValue());
            }
        }

        return name;
    }

    public static Character getFirstCharacter(String val) {
        return hasText(val) ? val.charAt(0) : null;
    }

    public static String[] getStringArray(String val, String delimiter) {
        return delimiter != null && hasText(val) ? val.split(Pattern.quote(delimiter)) : EMPTY_STRING_ARRAY;
    }

    public static int[] getIntegerArray(String val, String delimiter) {
        if (delimiter != null && hasText(val)) {
            String[] vl = val.split(Pattern.quote(delimiter));
            int[] res = new int[vl.length];

            for(int i = 0; i < res.length; ++i) {
                res[i] = getInt(vl[i]);
            }

            return res;
        } else {
            return EMPTY_INT_ARRAY;
        }
    }

    public static Integer getInteger(String val) {
        if (hasText(val)) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException var2) {
                LOGGER.warn("Cannot parse {} to Integer", val);
            }
        }

        return null;
    }

    public static int getInt(String val) {
        if (hasText(val)) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException var2) {
                LOGGER.warn("Cannot parse {} to int", val);
            }
        }

        return 0;
    }

    public static int getInt(String value, int defaultValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException var4) {
                LOGGER.warn("Cannot parse {} to int", value);
            }
        }

        return defaultValue;
    }

    public static Double getDouble(String val) {
        if (hasText(val)) {
            try {
                return Double.parseDouble(val.trim());
            } catch (NumberFormatException var2) {
                LOGGER.warn("Cannot parse {} to Double", val);
            }
        }

        return null;
    }

    public static String splitCamelCaseString(String s) {
        StringBuilder builder = new StringBuilder();
        String[] var2 = s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String w = var2[var4];
            builder.append(w);
            builder.append(' ');
        }

        return builder.toString().trim();
    }

    public static boolean hasLength(CharSequence str) {
        return str != null && str.length() > 0;
    }

    public static boolean hasLength(String str) {
        return hasLength((CharSequence)str);
    }

    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        } else {
            int strLen = str.length();

            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean hasText(String str) {
        return hasText((CharSequence)str);
    }

    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static List<String> splitSpaceExceptInQuotes(String s) {
        if (s == null) {
            return Collections.emptyList();
        } else {
            List<String> matchList = new ArrayList();
            Pattern patternSpaceExceptQuotes = Pattern.compile("'[^']*'|\"[^\"]*\"|( )");
            Matcher m = patternSpaceExceptQuotes.matcher(s);
            StringBuffer b = new StringBuffer();

            String arg;
            while(m.find()) {
                if (m.group(1) == null) {
                    m.appendReplacement(b, m.group(0));
                    arg = b.toString();
                    b.setLength(0);
                    if (hasText(arg)) {
                        matchList.add(arg);
                    }
                }
            }

            b.setLength(0);
            m.appendTail(b);
            arg = b.toString();
            if (hasText(arg)) {
                matchList.add(arg);
            }

            return matchList;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for(int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = HEX_DIGIT[v >>> 4];
            hexChars[j * 2 + 1] = HEX_DIGIT[v & 15];
        }

        return new String(hexChars);
    }

    public static String integerToHex(int val) {
        return Integer.toHexString(val).toUpperCase();
    }

    public static String bytesToMD5(byte[] val) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return bytesToHex(md.digest(val));
    }

    public static String getNullIfNull(Object object) {
        return object == null ? null : object.toString();
    }

    public static String getEmptyStringIfNull(Object object) {
        return object == null ? "" : object.toString();
    }

    public static String getEmptyStringIfNullEnum(Enum<?> object) {
        return object == null ? "" : object.name();
    }

    public static enum Suffix {
        NO(""),
        UNDERSCORE("_"),
        ONE_PTS("."),
        THREE_PTS("...");

        private final String value;

        private Suffix(String suffix) {
            this.value = suffix;
        }

        public String getValue() {
            return this.value;
        }

        public int getLength() {
            return this.value.length();
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
