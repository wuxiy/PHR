/*********************************************************************************
 * Copyright (c) 2019 中电健康云科技有限公司
 * 版本      DATE             BY             REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0    2020/1/16          wuxi           
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class LangUtil {
    private LangUtil() {
    }

    public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return (Iterable)(iterable == null ? Collections.emptyList() : iterable);
    }

    public static boolean getNULLtoFalse(Boolean val) {
        return val != null ? val : false;
    }

    public static boolean getNULLtoTrue(Boolean val) {
        return val != null ? val : true;
    }

    public static boolean getEmptytoFalse(String val) {
        return StringUtil.hasText(val) ? getBoolean(val) : false;
    }

    public static boolean geEmptytoTrue(String val) {
        return StringUtil.hasText(val) ? getBoolean(val) : true;
    }

    private static boolean getBoolean(String val) {
        return Boolean.TRUE.toString().equalsIgnoreCase(val);
    }

    public static <T, C extends Collection<T>> C convertCollectionType(Iterable<?> from, C newCollection, Class<T> listClass) {
        Iterator var3 = from.iterator();

        while(var3.hasNext()) {
            Object item = var3.next();
            newCollection.add(listClass.cast(item));
        }

        return newCollection;
    }

    public static Buffer safeBufferType(ByteBuffer buf) {
        return buf;
    }
}
