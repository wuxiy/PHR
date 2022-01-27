/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: GetService
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-16        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.service;

import cn.intcoder.phr.pacs.dcm4che3.getscu.CGet;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomNode;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomParam;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomProgress;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomState;
import org.dcm4che3.net.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

public class GetService {

    private static final Logger log = LoggerFactory.getLogger(GetService.class);

    public void getDicomFiles(String aet, String hostname, Integer port, File outputDir, DicomParam... keys) {
        DicomProgress progress = new DicomProgress();
        progress.addProgressListener(progress1 -> {
            if(log.isDebugEnabled()) {
                log.debug("Remaining operations:" + progress1.getNumberOfRemainingSuboperations());
            }
        });
        DicomNode calling = new DicomNode(aet);
        DicomNode called = new DicomNode(aet, hostname, port);
        DicomState state = CGet.process(calling, called, progress, outputDir, keys);
        // Should never happen
        if(Objects.isNull(state)) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("DICOM Status: {}", state.getStatus());
            log.debug(state.getMessage());
            log.debug("NumberOfRemainingSuboperations: {} " ,progress.getNumberOfRemainingSuboperations());
            log.debug("NumberOfCompletedSuboperations: {}", progress.getNumberOfCompletedSuboperations());
            log.debug("NumberOfFailedSuboperations: {}", progress.getNumberOfFailedSuboperations());
            log.debug("NumberOfWarningSuboperations: {}", progress.getNumberOfWarningSuboperations());
        }
        // see org.dcm4che3.net.Status
        // See server log at http://dicomserver.co.uk/logs/
        if (state.getStatus() != Status.Success) {
            log.error(state.getMessage());
        }
    }
}
