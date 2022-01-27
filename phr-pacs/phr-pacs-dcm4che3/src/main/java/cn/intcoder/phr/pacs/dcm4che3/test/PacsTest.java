/*********************************************************************************
 * Copyright (c) 2019 中电健康云科技有限公司
 * 版本      DATE             BY             REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0    2020/1/16          wuxi           
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.test;

import cn.intcoder.phr.pacs.dcm4che3.client.PacsClientAdmin;
import cn.intcoder.phr.pacs.dcm4che3.client.PacsClientInterface;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomParam;
import org.dcm4che3.data.Tag;

import java.io.File;

/**
 * @author: wuxi
 * @date: 2020/1/16
 */
public class PacsTest {

    public static void main(String[] strings) {
        DicomParam[] params = { new DicomParam(Tag.PatientID), new DicomParam(Tag.StudyInstanceUID),
                new DicomParam(Tag.NumberOfStudyRelatedSeries), new DicomParam(Tag.StudyDate, "20201012-"), new DicomParam(Tag.StudyTime, "090258-") };
        PacsClientInterface pacsClient = PacsClientAdmin.getInstance("172.16.56.45", 11112, "DCM4CHEE");
        File file = new File("/Users/xiwu/pacs/test");
        pacsClient.get(file, params);
    }


}
