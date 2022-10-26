package cn.ffcs.is.mss.analyzer.bean;/**
 * @title BbasDownloadCountWarnImproveEntity
 * @author PlatinaBoy
 * @date 2021-07-22 16:07
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @ClassName BbasDownloadCountWarnImproveEntity
 * @date 2021/7/22 16:07
 * @Author
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "bbas_download_count_warn_improve", schema = "SDFS", catalog = "")
@IdClass(BbasDownloadCountWarnImproveEntityPK.class)
public class BbasDownloadCountWarnImproveEntity {
    private Timestamp warnDatetime;
    private String username;
    private String companyName;
    private String departmentName;
    private String personalName;
    private String jobName;
    private Integer downloadCount;
    private String downloadSystem;
    private String downloadFileName;

    @Id
    @Column(name = "WARN_DATETIME")
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "COMPANY_NAME")
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Basic
    @Column(name = "DEPARTMENT_NAME")
    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Basic
    @Column(name = "PERSONAL_NAME")
    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }

    @Basic
    @Column(name = "JOB_NAME")
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Basic
    @Column(name = "DOWNLOAD_COUNT")
    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    @Id
    @Column(name = "DOWNLOAD_SYSTEM")
    public String getDownloadSystem() {
        return downloadSystem;
    }

    public void setDownloadSystem(String downloadSystem) {
        this.downloadSystem = downloadSystem;
    }

    @Basic
    @Column(name = "DOWNLOAD_FILEName")
    public String getDownloadFileName() {
        return downloadFileName;
    }

    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BbasDownloadCountWarnImproveEntity that = (BbasDownloadCountWarnImproveEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) && Objects.equals(username, that.username) && Objects.equals(companyName, that.companyName) && Objects.equals(departmentName, that.departmentName) && Objects.equals(personalName, that.personalName) && Objects.equals(jobName, that.jobName) && Objects.equals(downloadCount, that.downloadCount) && Objects.equals(downloadSystem, that.downloadSystem) && Objects.equals(downloadFileName, that.downloadFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, username, companyName, departmentName, personalName, jobName, downloadCount, downloadSystem, downloadFileName);
    }
}
