/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: ServiceUtil
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-15        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.util;

import cn.intcoder.phr.pacs.dcm4che3.param.DicomProgress;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomState;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class ServiceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtil.class);

    public enum ProgressStatus {
        FAILED, WARNING, COMPLETED
    }

    private ServiceUtil() {
    }

    public static void shutdownService(ExecutorService executorService) {
        if (executorService != null) {
            try {
                executorService.shutdown();
            } catch (Exception e) {
                LOGGER.error("ExecutorService shutdown", e);
            }
        }
    }

    public static void forceGettingAttributes(DicomState dcmState, AutoCloseable closeable) {
        DicomProgress p = dcmState.getProgress();
        if (p != null) {
            FileUtil.safeClose(closeable);
        }
    }

    public static void safeClose(DicomInputStream in) {
        if (in != null) {
            for (File file : in.getBulkDataFiles()) {
                FileUtil.delete(file);
            }
        }
    }

    public static void notifyProgession(DicomState state, String iuid, String cuid, int status, ProgressStatus ps,
        int numberOfSuboperations) {
        state.setStatus(status);
        DicomProgress p = state.getProgress();
        if (p != null) {
            Attributes cmd = Optional.ofNullable(p.getAttributes()).orElseGet(Attributes::new);
            cmd.setInt(Tag.Status, VR.US, status);
            cmd.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
            cmd.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
            notifyProgession(p, cmd, ps, numberOfSuboperations);
            p.setAttributes(cmd);
        }
    }

    public static void notifyProgession(DicomProgress p, Attributes cmd, ProgressStatus ps, int numberOfSuboperations) {
        if (p != null && cmd != null) {
            int c;
            int f;
            int r;
            int w;
            if (p.getAttributes() == null) {
                c = 0;
                f = 0;
                w = 0;
                r = numberOfSuboperations;
            } else {
                c = p.getNumberOfCompletedSuboperations();
                f = p.getNumberOfFailedSuboperations();
                w = p.getNumberOfWarningSuboperations();
                r = numberOfSuboperations - (c + f + w);
            }

            if (r < 1) {
                r = 1;
            }

            if (ps == ProgressStatus.COMPLETED) {
                c++;
            } else if (ps == ProgressStatus.FAILED) {
                f++;
            } else if (ps == ProgressStatus.WARNING) {
                w++;
            }
            cmd.setInt(Tag.NumberOfCompletedSuboperations, VR.US, c);
            cmd.setInt(Tag.NumberOfFailedSuboperations, VR.US, f);
            cmd.setInt(Tag.NumberOfWarningSuboperations, VR.US, w);
            cmd.setInt(Tag.NumberOfRemainingSuboperations, VR.US, r - 1);
        }
    }

    public static int getTotalOfSuboperations(Attributes cmd) {
        if (cmd != null) {
            int c = cmd.getInt(Tag.NumberOfCompletedSuboperations, 0);
            int f = cmd.getInt(Tag.NumberOfFailedSuboperations, 0);
            int w = cmd.getInt(Tag.NumberOfWarningSuboperations, 0);
            int r = cmd.getInt(Tag.NumberOfRemainingSuboperations, 0);
            return r + c + f + w;
        }
        return 0;
    }
}
