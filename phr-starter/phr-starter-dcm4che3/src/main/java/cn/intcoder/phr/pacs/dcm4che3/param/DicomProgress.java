/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: DicomProgress
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-15        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.param;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DicomProgress implements CancelListener {

    private final List<ProgressListener> listenerList;
    private Attributes attributes;
    private volatile boolean cancel;
    private File processedFile;
    private volatile boolean lastFailed = false;

    public DicomProgress() {
        this.cancel = false;
        this.listenerList = new ArrayList<>();
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        synchronized (this) {
            int failed = getNumberOfFailedSuboperations();
            failed = failed < 0 ? 0 : failed;
            this.attributes = attributes;
            lastFailed = failed < getNumberOfFailedSuboperations();
        }

        fireProgress();
    }

    public boolean isLastFailed() {
        return lastFailed;
    }

    public synchronized File getProcessedFile() {
        return processedFile;
    }

    public synchronized void setProcessedFile(File processedFile) {
        this.processedFile = processedFile;
    }

    public void addProgressListener(ProgressListener listener) {
        if (listener != null && !listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    public void removeProgressListener(ProgressListener listener) {
        if (listener != null) {
            listenerList.remove(listener);
        }
    }

    private void fireProgress() {
        for (ProgressListener progressListener : listenerList) {
            progressListener.handleProgression(this);
        }
    }

    @Override
    public void cancel() {
        this.cancel = true;
    }

    public boolean isCancel() {
        return cancel;
    }

    private int getIntTag(int tag) {
        Attributes dcm = attributes;
        if (dcm == null) {
            return -1;
        }
        return dcm.getInt(tag, -1);
    }

    public int getStatus() {
        if (isCancel()) {
            return Status.Cancel;
        }
        Attributes dcm = attributes;
        if (dcm == null) {
            return Status.Pending;
        }
        return dcm.getInt(Tag.Status, Status.Pending);
    }

    public String getErrorComment() {
        Attributes dcm = attributes;
        if (dcm == null) {
            return null;
        }
        return dcm.getString(Tag.ErrorComment);
    }

    public int getNumberOfRemainingSuboperations() {
        return getIntTag(Tag.NumberOfRemainingSuboperations);
    }

    public int getNumberOfCompletedSuboperations() {
        return getIntTag(Tag.NumberOfCompletedSuboperations);
    }

    public int getNumberOfFailedSuboperations() {
        return getIntTag(Tag.NumberOfFailedSuboperations);
    }

    public int getNumberOfWarningSuboperations() {
        return getIntTag(Tag.NumberOfWarningSuboperations);
    }

}
