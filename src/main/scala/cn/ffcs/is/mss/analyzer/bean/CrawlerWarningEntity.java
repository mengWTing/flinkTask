package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "crawler_warning", schema = "sdfs", catalog = "")
@IdClass(CrawlerWarningEntityPK.class)
public class CrawlerWarningEntity {
    private String userName;
    private Timestamp startDatetime;
    private Timestamp endDatetime;
    private Long conncount;
    private String loginPlace;
    private String loginSystem;
    private String sourceIp;
    private String destinationIp;

    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Id
    @Column(name = "START_DATETIME")
    public Timestamp getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Timestamp startDatetime) {
        this.startDatetime = startDatetime;
    }

    @Basic
    @Column(name = "END_DATETIME")
    public Timestamp getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(Timestamp endDatetime) {
        this.endDatetime = endDatetime;
    }

    @Basic
    @Column(name = "CONNCOUNT")
    public Long getConncount() {
        return conncount;
    }

    public void setConncount(Long conncount) {
        this.conncount = conncount;
    }

    @Basic
    @Column(name = "LOGIN_PLACE")
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "LOGIN_SYSTEM")
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
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
    @Column(name = "DESTINATION_IP")
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrawlerWarningEntity that = (CrawlerWarningEntity) o;

        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (startDatetime != null ? !startDatetime.equals(that.startDatetime) : that.startDatetime != null)
            return false;
        if (endDatetime != null ? !endDatetime.equals(that.endDatetime) : that.endDatetime != null) return false;
        if (conncount != null ? !conncount.equals(that.conncount) : that.conncount != null) return false;
        if (loginPlace != null ? !loginPlace.equals(that.loginPlace) : that.loginPlace != null) return false;
        if (loginSystem != null ? !loginSystem.equals(that.loginSystem) : that.loginSystem != null) return false;
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) return false;
        if (destinationIp != null ? !destinationIp.equals(that.destinationIp) : that.destinationIp != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (startDatetime != null ? startDatetime.hashCode() : 0);
        result = 31 * result + (endDatetime != null ? endDatetime.hashCode() : 0);
        result = 31 * result + (conncount != null ? conncount.hashCode() : 0);
        result = 31 * result + (loginPlace != null ? loginPlace.hashCode() : 0);
        result = 31 * result + (loginSystem != null ? loginSystem.hashCode() : 0);
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (destinationIp != null ? destinationIp.hashCode() : 0);
        return result;
    }
}
