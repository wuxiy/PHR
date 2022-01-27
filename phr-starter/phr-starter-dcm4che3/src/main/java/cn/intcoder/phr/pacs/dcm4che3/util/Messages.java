/*********************************************************************************
 * Copyright (c) 2019 中电健康云科技有限公司
 * 版本      DATE             BY             REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0    2020/1/16          wuxi           
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "messages";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException var2) {
            return '!' + key + '!';
        }
    }
}