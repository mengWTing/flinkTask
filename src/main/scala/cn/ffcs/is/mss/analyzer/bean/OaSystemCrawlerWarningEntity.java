package cn.ffcs.is.mss.analyzer.bean;/**
 * @title OaSystemCrawlerWarningEntity
 * @author PlatinaBoy
 * @date 2021-07-15 16:57
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @ClassName OaSystemCrawlerWarningEntity
 * @date 2021/7/15 16:57
 * @Author
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "oa_system_crawler_warning", schema = "SDFS", catalog = "")
@IdClass(OaSystemCrawlerWarningEntityPK.class)
public class OaSystemCrawlerWarningEntity {
    private Timestamp startTime;
    private Timestamp endTime;
    private String userName;
    private String sourceIp;
    private String desIp;
    private Integer isOm;
    private String officalUrl;
    private String addrUrl;
    private String companyName;
    private String departmentName;
    private String personalName;
    private String jobName;
    private Long count;

    @Id
    @Column(name = "START_TIME")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "END_TIME")
    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "SOURCE_IP")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "DES_IP")
    public String getDesIp() {
        return desIp;
    }

    public void setDesIp(String desIp) {
        this.desIp = desIp;
    }

    @Basic
    @Column(name = "IS_OM")
    public Integer getIsOm() {
        return isOm;
    }

    public void setIsOm(Integer isOm) {
        this.isOm = isOm;
    }

    @Basic
    @Column(name = "OFFICAL_URL")
    public String getOfficalUrl() {
        return officalUrl;
    }

    public void setOfficalUrl(String officalUrl) {
        this.officalUrl = officalUrl;
    }

    @Basic
    @Column(name = "ADDR_URL")
    public String getAddrUrl() {
        return addrUrl;
    }

    public void setAddrUrl(String addrUrl) {
        this.addrUrl = addrUrl;
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
    @Column(name = "COUNT")
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OaSystemCrawlerWarningEntity that = (OaSystemCrawlerWarningEntity) o;
        return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime) && Objects.equals(userName, that.userName) && Objects.equals(sourceIp, that.sourceIp) && Objects.equals(desIp, that.desIp) && Objects.equals(isOm, that.isOm) && Objects.equals(officalUrl, that.officalUrl) && Objects.equals(addrUrl, that.addrUrl) && Objects.equals(companyName, that.companyName) && Objects.equals(departmentName, that.departmentName) && Objects.equals(personalName, that.personalName) && Objects.equals(jobName, that.jobName) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, userName, sourceIp, desIp, isOm, officalUrl, addrUrl, companyName, departmentName, personalName, jobName, count);
    }
}
