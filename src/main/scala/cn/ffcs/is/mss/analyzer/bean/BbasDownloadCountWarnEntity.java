/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-01-22 16:43:24
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @author chenwei
 * @date 2019-01-22 16:43:24
 * @title BbasDownloadCountWarnEntity
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
@Entity
@Table(name = "BBAS_DOWNLOAD_COUNT_WARN", schema = "SDFS", catalog = "")
@IdClass(BbasDownloadCountWarnEntityPK.class)
public class BbasDownloadCountWarnEntity {

    private Timestamp warnDatetime;
    private String username;
    private String companyName;
    private String departmentName;
    private String personalName;
    private String jobName;
    private Integer downloadCount;
    private String downloadSystem;

    @Id
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "USERNAME", nullable = false, length = 45)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "COMPANY_NAME", nullable = true, length = 45)
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Basic
    @Column(name = "DEPARTMENT_NAME", nullable = true, length = 45)
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

    @Basic
    @Column(name = "DOWNLOAD_COUNT", nullable = true)
    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    @Id
    @Column(name = "DOWNLOAD_SYSTEM", nullable = false, length = 45)
    public String getDownloadSystem() {
        return downloadSystem;
    }

    public void setDownloadSystem(String downloadSystem) {
        this.downloadSystem = downloadSystem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasDownloadCountWarnEntity that = (BbasDownloadCountWarnEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(username, that.username) &&
            Objects.equals(companyName, that.companyName) &&
            Objects.equals(departmentName, that.departmentName) &&
            Objects.equals(personalName, that.personalName) &&
            Objects.equals(jobName, that.jobName) &&
            Objects.equals(downloadCount, that.downloadCount) &&
            Objects.equals(downloadSystem, that.downloadSystem);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(warnDatetime, username, companyName, departmentName, personalName, jobName,
                downloadCount, downloadSystem);
    }
}
