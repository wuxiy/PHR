/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: DicomState
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-15        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.param;

import cn.intcoder.phr.pacs.dcm4che3.util.StringUtil;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Status;

import java.util.ArrayList;
import java.util.List;

public class DicomState {
    private volatile int status;
    private String message;
    private final List<Attributes> dicomRSP;
    private final DicomProgress progress;

    public DicomState() {
        this(Status.Pending, null, null);
    }

    public DicomState(DicomProgress progress) {
        this(Status.Pending, null, progress);
    }

    public DicomState(int status, String message, DicomProgress progress) {
        this.status = status;
        this.message = message;
        this.progress = progress;
        this.dicomRSP = new ArrayList<>();
    }

    /**
     * Get the DICOM status
     * 
     * @ see org.dcm4che3.net.Status
     * 
     * @return the DICOM status of the process
     */
    public int getStatus() {
        if (progress != null && progress.getAttributes() != null) {
            return progress.getStatus();
        }
        return status;
    }

    /**
     * Set the DICOM status
     * 
     * @ see org.dcm4che3.net.Status
     * 
     * @param status
     *            DICOM status of the process
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public DicomProgress getProgress() {
        return progress;
    }

    public List<Attributes> getDicomRSP() {
        return dicomRSP;
    }

    public void addDicomRSP(Attributes dicomRSP) {
        if (dicomRSP != null) {
            this.dicomRSP.add(dicomRSP);
        }
    }

    public static DicomState buildMessage(DicomState dcmState, String timeMessage, Exception e) {
        DicomState state = dcmState;
        if (state == null) {
            state = new DicomState(Status.UnableToProcess, null, null);
        }

        DicomProgress p = state.getProgress();
        int s = state.getStatus();

        StringBuilder msg = new StringBuilder();

        boolean hasFailed = false;
        if (p != null) {
            int failed = p.getNumberOfFailedSuboperations();
            int warning = p.getNumberOfWarningSuboperations();
            int remaining = p.getNumberOfRemainingSuboperations();
            if (failed > 0) {
                hasFailed = true;
                msg.append(String.format("%d/%d operations has failed.", failed,
                    failed + p.getNumberOfCompletedSuboperations()));
            } else if (remaining > 0) {
                msg.append(String.format("%d operations remains. ", remaining));
            } else if (warning > 0) {
                msg.append(String.format("%d operations has a warning status. ", warning));
            }
        }
        if (e != null) {
            hasFailed = true;
            if (msg.length() > 0) {
                msg.append(" ");
            }
            msg.append(e.getLocalizedMessage());
        }

        if (p != null && p.getAttributes() != null) {
            String error = p.getErrorComment();
            if (StringUtil.hasText(error)) {
                hasFailed = true;
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM error");
                msg.append(StringUtil.COLON_AND_SPACE);
                msg.append(error);
            }

            if (!Status.isPending(s) && s != -1 && s != Status.Success && s != Status.Cancel) {
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM status");
                msg.append(StringUtil.COLON_AND_SPACE);
                msg.append(s);
            }
        }

        if (!hasFailed) {
            if (timeMessage != null) {
                msg.append(timeMessage);
            }
        } else {
            if (Status.isPending(s) || s == -1) {
                state.setStatus(Status.UnableToProcess);
            }
        }
        state.setMessage(msg.toString());
        return state;
    }

}
