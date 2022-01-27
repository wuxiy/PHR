#### PACS系统查询保存
+ 提供条件查询给定时间段的STUDY唯一标志集合
+ 根据唯一STUDY标志返回DICOM源文件到本地
#### 调用举例

1. C-FIND和C-GET
```
        DicomParam[] params = { new DicomParam(Tag.PatientID), new DicomParam(Tag.StudyInstanceUID),
                new DicomParam(Tag.NumberOfStudyRelatedSeries), new DicomParam(Tag.StudyDate, "20061012-"), new DicomParam(Tag.StudyTime, "090258-") };
        PacsClientInterface pacsClient = PacsClientAdmin.getInstance("172.16.56.45", 11112, "DCM4CHEE");
        File file = new File("/Users/xiwu/pacs");
        pacsClient.get(file, params);
```
2.DCM4CHEE提供的rest方法


