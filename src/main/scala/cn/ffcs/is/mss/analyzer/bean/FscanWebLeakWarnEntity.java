package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/3/14 9:26
 * @Description:
 * @update:
 */
@Entity
@Table(name = "fscan_web_leak", schema = "SDFS", catalog = "")
public class FscanWebLeakWarnEntity {
    private Timestamp alertTime;
    private String userName;
    private String sourceIp;
    private String sourcePort;
    private String destinationIp;
    private String destinationPort;
    private String loginPlace;
    private String loginSystem;
    private String loginMajor;
    private String requestType;
    private String requestContent;
    private String url;
    private String alertName;
    private String alertType;

    @Basic
    @Column(name = "alert_time")
    public Timestamp getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Timestamp alertTime) {
        this.alertTime = alertTime;
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
    @Column(name = "source_ip")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "source_port")
    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
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
    @Column(name = "destination_port")
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "login_place")
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "login_system")
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "login_major")
    public String getLoginMajor() {
        return loginMajor;
    }

    public void setLoginMajor(String loginMajor) {
        this.loginMajor = loginMajor;
    }

    @Basic
    @Column(name = "request_type")
    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @Basic
    @Column(name = "request_content")
    public String getRequestContent() {
        return requestContent;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    @Basic
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "alert_name")
    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    @Basic
    @Column(name = "alert_type")
    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FscanWebLeakWarnEntity that = (FscanWebLeakWarnEntity) o;
        return Objects.equals(alertTime, that.alertTime) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(sourcePort, that.sourcePort) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(destinationPort, that.destinationPort) &&
                Objects.equals(loginPlace, that.loginPlace) &&
                Objects.equals(loginSystem, that.loginSystem) &&
                Objects.equals(loginMajor, that.loginMajor) &&
                Objects.equals(requestType, that.requestType) &&
                Objects.equals(requestContent, that.requestContent) &&
                Objects.equals(url, that.url) &&
                Objects.equals(alertName, that.alertName) &&
                Objects.equals(alertType, that.alertType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertTime, userName, sourceIp, sourcePort, destinationIp, destinationPort, loginPlace, loginSystem, loginMajor, requestType, requestContent, url, alertName, alertType);
    }
}
