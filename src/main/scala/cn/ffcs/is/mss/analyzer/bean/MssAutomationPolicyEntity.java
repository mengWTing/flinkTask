package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @ClassName MssAutomationPolicyEntity
 * @date 2022/1/18 11:27
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "mss_automation_policy", schema = "bigdata", catalog = "")
public class MssAutomationPolicyEntity {
    private long id;
    private String userName;
    private String metaIp;
    private String destinationIp;
    private String alarmDetail;
    private Timestamp alarmTime;
    private Timestamp createTime;
    private String businessSystem;
    private String domainName;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "user_name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "meta_ip")
    public String getMetaIp() {
        return metaIp;
    }

    public void setMetaIp(String metaIp) {
        this.metaIp = metaIp;
    }

    @Basic
    @Column(name = "destination_ip")
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "alarm_detail")
    public String getAlarmDetail() {
        return alarmDetail;
    }

    public void setAlarmDetail(String alarmDetail) {
        this.alarmDetail = alarmDetail;
    }

    @Basic
    @Column(name = "alarm_time")
    public Timestamp getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Timestamp alarmTime) {
        this.alarmTime = alarmTime;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "business_system")
    public String getBusinessSystem() {
        return businessSystem;
    }

    public void setBusinessSystem(String businessSystem) {
        this.businessSystem = businessSystem;
    }

    @Basic
    @Column(name = "domain_name")
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MssAutomationPolicyEntity that = (MssAutomationPolicyEntity) o;
        return id == that.id && Objects.equals(userName, that.userName) && Objects.equals(metaIp, that.metaIp) && Objects.equals(destinationIp, that.destinationIp) && Objects.equals(alarmDetail, that.alarmDetail) && Objects.equals(alarmTime, that.alarmTime) && Objects.equals(createTime, that.createTime) && Objects.equals(businessSystem, that.businessSystem) && Objects.equals(domainName, that.domainName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, metaIp, destinationIp, alarmDetail, alarmTime, createTime, businessSystem, domainName);
    }
}
