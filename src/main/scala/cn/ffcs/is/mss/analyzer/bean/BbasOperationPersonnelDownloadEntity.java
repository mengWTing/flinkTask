package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/11/21 19:04
 * @Modified By
 */
@Entity
@Table(name = "BBAS_OPERATION_PERSONNEL_DOWNLOAD", schema = "SDFS", catalog = "")
@IdClass(BbasOperationPersonnelDownloadEntityPK.class)
public class BbasOperationPersonnelDownloadEntity {

    private Timestamp warnDatetime;
    private String username;
    private String companyName;
    private String departmentName;
    private String personalName;
    private String jobName;
    private String downloadFimeName;
    private String downloadSystem;
    private byte[] downloadFileSize;
    private String sourceIp;
    private String destinationIp;
    private String url;

    @Id
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "USERNAME", nullable = false, length = 100)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "COMPANY_NAME", nullable = true, length = 200)
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Basic
    @Column(name = "DEPARTMENT_NAME", nullable = true, length = 200)
    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Basic
    @Column(name = "PERSONAL_NAME", nullable = true, length = 45)
    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }

    @Basic
    @Column(name = "JOB_NAME", nullable = true, length = 45)
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Id
    @Column(name = "DOWNLOAD_FIME_NAME", nullable = false, length = 45)
    public String getDownloadFimeName() {
        return downloadFimeName;
    }

    public void setDownloadFimeName(String downloadFimeName) {
        this.downloadFimeName = downloadFimeName;
    }

    @Basic
    @Column(name = "DOWNLOAD_SYSTEM", nullable = true, length = 45)
    public String getDownloadSystem() {
        return downloadSystem;
    }

    public void setDownloadSystem(String downloadSystem) {
        this.downloadSystem = downloadSystem;
    }

    @Basic
    @Column(name = "DOWNLOAD_FILE_SIZE", nullable = true)
    public byte[] getDownloadFileSize() {
        return downloadFileSize;
    }

    public void setDownloadFileSize(byte[] downloadFileSize) {
        this.downloadFileSize = downloadFileSize;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = true, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = true, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "URL", nullable = true, length = 1000)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasOperationPersonnelDownloadEntity that = (BbasOperationPersonnelDownloadEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(username, that.username) &&
            Objects.equals(companyName, that.companyName) &&
            Objects.equals(departmentName, that.departmentName) &&
            Objects.equals(personalName, that.personalName) &&
            Objects.equals(jobName, that.jobName) &&
            Objects.equals(downloadFimeName, that.downloadFimeName) &&
            Objects.equals(downloadSystem, that.downloadSystem) &&
            Arrays.equals(downloadFileSize, that.downloadFileSize) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        int result = Objects
            .hash(warnDatetime, username, companyName, departmentName, personalName, jobName,
                downloadFimeName, downloadSystem, sourceIp, destinationIp, url);
        result = 31 * result + Arrays.hashCode(downloadFileSize);
        return result;
    }
}
