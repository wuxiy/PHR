/*********************************************************************************
 * Copyright (c) 2019 中电健康云科技有限公司
 * 版本      DATE             BY             REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0    2020/1/15          wuxi           
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.service;

import cn.intcoder.phr.pacs.dcm4che3.findscu.CFind;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomNode;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomParam;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomState;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FindService {

    private final static Logger log = LoggerFactory.getLogger(FindService.class);

    public List<String> getStudyInstanceUIDs(String aet, String hostname, Integer port, DicomParam... keys) {
        List<String> results = new ArrayList<>();
        DicomNode calling = new DicomNode(aet);
        DicomNode called = new DicomNode(aet, hostname, port);
        DicomState state = CFind.process(calling, called, keys);
        // Should never happen
        if(Objects.isNull(state)) {
            return null;
        }
        List<Attributes> items = state.getDicomRSP();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Attributes item = items.get(i);
                String studyInstanceUID = item.getString(Tag.StudyInstanceUID);
                if(log.isDebugEnabled()) {
                    log.debug("CFind Item StudyInstanceUID: {} ", studyInstanceUID);
                }
                results.add(studyInstanceUID);
            }
        }
        if(log.isDebugEnabled()) {
            log.debug("DICOM Status: {}" ,state.getStatus());
            log.debug("DICOM state message: {}", state.getMessage());
        }
        // see org.dcm4che3.net.Status
        // See server log at http://dicomserver.co.uk/logs/
        if(state.getStatus() != Status.Success) {
            log.error(state.getMessage());
        }
        if(state.getDicomRSP().isEmpty()) {
            log.error("No DICOM RSP Object");
        }
        return results;
    }
}
