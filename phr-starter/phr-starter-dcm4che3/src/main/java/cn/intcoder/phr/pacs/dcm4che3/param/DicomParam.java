/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: DicomParam
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-15        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.param;

import org.dcm4che3.data.ElementDictionary;

public class DicomParam {

    private final int tag;
    private final String[] values;
    private final int[] parentSeqTags;

    public DicomParam(int tag, String... values) {
        this(null, tag, values);
    }

    public DicomParam(int[] parentSeqTags, int tag, String... values) {
        this.tag = tag;
        this.values = values;
        this.parentSeqTags = parentSeqTags;
    }

    public int getTag() {
        return tag;
    }

    public String[] getValues() {
        return values;
    }

    public int[] getParentSeqTags() {
        return parentSeqTags;
    }

    public String getTagName() {
        return ElementDictionary.keywordOf(tag, null);
    }

}
