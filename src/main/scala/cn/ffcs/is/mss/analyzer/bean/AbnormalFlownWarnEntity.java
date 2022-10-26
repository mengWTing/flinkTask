package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @author liangzhaosuo
 * @title AbnormalFlownWarnEntity
 * @date 2020-08-17 15:41
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */


@Entity
@Table(name = "ABNORMAL_FLOWN_WARN", schema = "SDFS", catalog = "")
public class AbnormalFlownWarnEntity {
    private Timestamp warnTime;
    private String userName;
    private String sourceIp;
    private String destinationIp;
    private Integer warnType;
    private Long octets;

    @Basic
    @Column(name = "WARN_TIME", nullable = true)
    public Timestamp getWarnTime() {
        return warnTime;
    }

    public void setWarnTime(Timestamp warnTime) {
        this.warnTime = warnTime;
    }

    @Basic
    @Column(name = "USER_NAME", nullable = true, length = 255)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = true, length = 255)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = true, length = 255)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "WARN_TYPE", nullable = true)
    public Integer getWarnType() {
        return warnType;
    }

    public void setWarnType(Integer warnType) {
        this.warnType = warnType;
    }

    @Basic
    @Column(name = "OCTETS", nullable = true)
    public Long getOctets() {
        return octets;
    }

    public void setOctets(Long octets) {
        this.octets = octets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbnormalFlownWarnEntity that = (AbnormalFlownWarnEntity) o;

        if (warnTime != null ? !warnTime.equals(that.warnTime) : that.warnTime != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) return false;
        if (destinationIp != null ? !destinationIp.equals(that.destinationIp) : that.destinationIp != null)
            return false;
        if (warnType != null ? !warnType.equals(that.warnType) : that.warnType != null) return false;
        if (octets != null ? !octets.equals(that.octets) : that.octets != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnTime != null ? warnTime.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (destinationIp != null ? destinationIp.hashCode() : 0);
        result = 31 * result + (warnType != null ? warnType.hashCode() : 0);
        result = 31 * result + (octets != null ? octets.hashCode() : 0);
        return result;
    }
}
