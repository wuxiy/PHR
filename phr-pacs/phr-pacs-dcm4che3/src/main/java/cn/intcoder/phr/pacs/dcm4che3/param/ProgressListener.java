/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: ProgressListener
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-15        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.param;

public interface ProgressListener {

    void handleProgression(DicomProgress progress);

}