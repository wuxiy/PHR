/*********************************************************************************
 * Copyright (c)2020 CEC Health
 * FILE: CS3ClientInterface
 * 版本      DATE             BY               REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0   2020-01-16        xiwu
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.client;

import cn.intcoder.phr.pacs.dcm4che3.param.DicomParam;

import java.io.File;
import java.util.List;

/**
 * non-singleton
 * @Author: wuxi
 * @Date: 2019/3/14
 */
public interface PacsClientInterface {

    /**
     *
     * @param hostname
     * @param port
     * @param aet
     * @return
     */
    PacsClientInterface init(String hostname, Integer port, String aet);

    /**
     * 获取dicom原文件
     * @param outputDir 输出目录
     * @param keys 查询参数
     * @return 返回studyInstanceUID列表
     */
    List<String> get(File outputDir, DicomParam... keys);


    /**
     * 获取dicom原文件
     * @param outputDir 输出目录
     * @param keys 查询参数
     * @return 返回studyInstanceUID列表
     */
    List<String> getByRest(File outputDir, DicomParam... keys);

}
