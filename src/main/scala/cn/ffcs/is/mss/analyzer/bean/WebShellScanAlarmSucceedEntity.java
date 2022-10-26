package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "WEB_SHELL_SCAN_ALARM_SUCCEED", schema = "SDFS", catalog = "")
public class WebShellScanAlarmSucceedEntity {
    private String userName;
    private String sourceIp;
    private String attackUrl;
    private Timestamp attackTime;
    private Integer dangerLevel;
    private Integer httpStatus;
    private Integer webLen;
    private String destinationIp;
    private String url;

    @Basic
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
    @Column(name = "ATTACK_URL")
    public String getAttackUrl() {
        return attackUrl;
    }

    public void setAttackUrl(String attackUrl) {
        this.attackUrl = attackUrl;
    }

    @Basic
    @Column(name = "ATTACK_TIME")
    public Timestamp getAttackTime() {
        return attackTime;
    }

    public void setAttackTime(Timestamp attackTime) {
        this.attackTime = attackTime;
    }

    @Basic
    @Column(name = "DANGER_LEVEL")
    public Integer getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(Integer dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    @Basic
    @Column(name = "HTTP_STATUS")
    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Basic
    @Column(name = "WEB_LEN")
    public Integer getWebLen() {
        return webLen;
    }

    public void setWebLen(Integer webLen) {
        this.webLen = webLen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebShellScanAlarmSucceedEntity that = (WebShellScanAlarmSucceedEntity) o;

        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) return false;
        if (attackUrl != null ? !attackUrl.equals(that.attackUrl) : that.attackUrl != null) return false;
        if (attackTime != null ? !attackTime.equals(that.attackTime) : that.attackTime != null) return false;
        if (dangerLevel != null ? !dangerLevel.equals(that.dangerLevel) : that.dangerLevel != null) return false;
        if (httpStatus != null ? !httpStatus.equals(that.httpStatus) : that.httpStatus != null) return false;
        if (webLen != null ? !webLen.equals(that.webLen) : that.webLen != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (attackUrl != null ? attackUrl.hashCode() : 0);
        result = 31 * result + (attackTime != null ? attackTime.hashCode() : 0);
        result = 31 * result + (dangerLevel != null ? dangerLevel.hashCode() : 0);
        result = 31 * result + (httpStatus != null ? httpStatus.hashCode() : 0);
        result = 31 * result + (webLen != null ? webLen.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "DESTINATION_IP")
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "URL")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
