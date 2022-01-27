/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: PacsClientAdmin
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-16        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.client;


import cn.intcoder.phr.pacs.dcm4che3.param.DicomParam;
import cn.intcoder.phr.pacs.dcm4che3.service.FindService;
import cn.intcoder.phr.pacs.dcm4che3.service.GetService;
import org.dcm4che3.data.Tag;

import java.io.File;
import java.util.List;

public enum PacsClientAdmin implements PacsClientInterface {
    INSTANCE {
        @Override
        public PacsClientInterface init(String hostname, Integer port, String aet) {
            build(hostname, port, aet);
            return this;
        }

        @Override
        public List<String> get(File outputDir, DicomParam... keys) {
            FindService findService = new FindService();
            GetService getService = new GetService();
            List<String> studyInstanceUIDS = findService.getStudyInstanceUIDs(aet, hostname, port, keys);
            if(!studyInstanceUIDS.isEmpty()) {
                for (String studyInstanceUID : studyInstanceUIDS) {
                    DicomParam[] params = {new DicomParam(Tag.StudyInstanceUID, studyInstanceUID)};
                    File newDir = new File(outputDir.getPath() + File.separator + studyInstanceUID);
                    if (!newDir.exists()) {
                        newDir.mkdir();
                    }
                    getService.getDicomFiles(aet, hostname, port, newDir, params);
                }
            }
            return studyInstanceUIDS;
        }

        @Override
        public List<String> getByRest(File outputDir, DicomParam... keys) {
            return null;
        }

        private String aet;

        /**
         * host
         */
        private String hostname;

        private Integer port;

        private String FIND_URL = "http://%s:%s/dcm4chee-arc/aets/%s/rs/studies?includefield=all&offset=0&StudyDate=%s&StudyTime=%s@&orderby=-StudyDate,-StudyTime&returnempty=false&compressionfailed=false";

        public void build(String hostname, Integer port, String aet) {
            this.hostname = hostname;
            this.port = port;
            this.aet = aet;
        }

    };


    public static PacsClientInterface getInstance(String hostname, Integer port, String aet) {

        return PacsClientAdmin.INSTANCE.init(hostname, port, aet);
    }

}
