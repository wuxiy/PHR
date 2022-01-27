package cn.intcoder.phr.pacs.dcm4che3.getscu;

import cn.intcoder.phr.pacs.dcm4che3.common.CLIUtils;
import cn.intcoder.phr.pacs.dcm4che3.param.AdvancedParams;
import cn.intcoder.phr.pacs.dcm4che3.param.DeviceOpService;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomNode;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomParam;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomProgress;
import cn.intcoder.phr.pacs.dcm4che3.param.DicomState;
import cn.intcoder.phr.pacs.dcm4che3.util.FileUtil;
import cn.intcoder.phr.pacs.dcm4che3.util.ServiceUtil;
import cn.intcoder.phr.pacs.dcm4che3.util.StringUtil;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;

public class CGet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CGet.class);

    private CGet() {
    }

    /**
     * @param callingNode
     *            the calling DICOM node configuration
     * @param calledNode
     *            the called DICOM node configuration
     * @param progress
     *            the progress handler
     * @param keys
     *            the matching and returning keys. DicomParam with no value is a returning key.
     * @return The DicomSate instance which contains the DICOM response, the DICOM status, the error message and the
     *         progression.
     */
    public static DicomState process(DicomNode callingNode, DicomNode calledNode, DicomProgress progress,
                                     File outputDir, DicomParam... keys) {
        return process(null, callingNode, calledNode, progress, outputDir, keys);
    }

    /**
     * @param params
     *            the optional advanced parameters (proxy, authentication, connection and TLS)
     * @param callingNode
     *            the calling DICOM node configuration
     * @param calledNode
     *            the called DICOM node configuration
     * @param progress
     *            the progress handler
     * @param keys
     *            the matching and returning keys. DicomParam with no value is a returning key.
     * @return The DicomSate instance which contains the DICOM response, the DICOM status, the error message and the
     *         progression.
     */
    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode,
                                     DicomProgress progress, File outputDir, DicomParam... keys) {
        return process(params, callingNode, calledNode, progress, outputDir, null, keys);
    }

    /**
     * @param params
     *            the optional advanced parameters (proxy, authentication, connection and TLS)
     * @param callingNode
     *            the calling DICOM node configuration
     * @param calledNode
     *            the called DICOM node configuration
     * @param progress
     *            the progress handler
     * @param keys
     *            the matching and returning keys. DicomParam with no value is a returning key.
     * @return The DicomSate instance which contains the DICOM response, the DICOM status, the error message and the
     *         progression.
     */
    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode,
        DicomProgress progress, File outputDir, URL sopClassURL, DicomParam... keys) {
        if (callingNode == null || calledNode == null || outputDir == null) {
            throw new IllegalArgumentException("callingNode, calledNode or outputDir cannot be null!");
        }
        GetSCU getSCU = null;
        AdvancedParams options = params == null ? new AdvancedParams() : params;

        try {
            getSCU = new GetSCU(progress);
            Connection remote = getSCU.getRemoteConnection();
            Connection conn = getSCU.getConnection();
            options.configureConnect(getSCU.getAAssociateRQ(), remote, calledNode);
            options.configureBind(getSCU.getApplicationEntity(), conn, callingNode);
            DeviceOpService service = new DeviceOpService(getSCU.getDevice());

            // configure
            options.configure(conn);
            options.configureTLS(conn, remote);

            getSCU.setPriority(options.getPriority());

            getSCU.setStorageDirectory(outputDir);

            getSCU.setInformationModel(getInformationModel(options), options.getTsuidOrder(),
                options.getQueryOptions().contains(QueryOption.RELATIONAL));

            configureRelatedSOPClass(getSCU, sopClassURL);

            for (DicomParam p : keys) {
                getSCU.addKey(p.getTag(), p.getValues());
            }

            service.start();
            try {
                DicomState dcmState = getSCU.getState();
                long t1 = System.currentTimeMillis();
                getSCU.open();
                long t2 = System.currentTimeMillis();
                getSCU.retrieve();
                ServiceUtil.forceGettingAttributes(dcmState, getSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg =
                    MessageFormat.format("DICOM C-GET connected in {2}ms from {0} to {1}. Get files in {3}ms.",
                        getSCU.getAAssociateRQ().getCallingAET(), getSCU.getAAssociateRQ().getCalledAET(), t2 - t1,
                        t3 - t2);
                return DicomState.buildMessage(dcmState, timeMsg, null);
            } catch (Exception e) {
                LOGGER.error("getscu", e);
                ServiceUtil.forceGettingAttributes(getSCU.getState(), getSCU);
                return DicomState.buildMessage(getSCU.getState(), null, e);
            } finally {
                FileUtil.safeClose(getSCU);
                service.stop();
            }
        } catch (Exception e) {
            LOGGER.error("getscu", e);
            return new DicomState(Status.UnableToProcess,
                "DICOM Get failed" + StringUtil.COLON_AND_SPACE + e.getMessage(), null);
        }
    }

    private static void configureRelatedSOPClass(GetSCU getSCU, URL url) throws IOException {
        Properties p = new Properties();
        try {
            if (url == null) {
                p.load(getSCU.getClass().getResourceAsStream("store-tcs.properties"));
            } else {
                p.load(url.openStream());
            }
            for (Entry<Object, Object> entry : p.entrySet()) {
                configureStorageSOPClass(getSCU, (String) entry.getKey(), (String) entry.getValue());
            }
        } catch (Exception e) {
            LOGGER.error("Read sop classes", e);
        }
    }

    private static void configureStorageSOPClass(GetSCU getSCU, String cuid, String tsuids) {
        String[] ts = StringUtils.split(tsuids, ';');
        for (int i = 0; i < ts.length; i++) {
            ts[i] = CLIUtils.toUID(ts[i]);
        }
        getSCU.addOfferedStorageSOPClass(CLIUtils.toUID(cuid), ts);
    }

    private static GetSCU.InformationModel getInformationModel(AdvancedParams options) {
        Object model = options.getInformationModel();
        if (model instanceof GetSCU.InformationModel) {
            return (GetSCU.InformationModel) model;
        }
        return GetSCU.InformationModel.StudyRoot;
    }

}
