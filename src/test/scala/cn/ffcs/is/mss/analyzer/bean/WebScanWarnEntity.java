package scala.cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/3/14 9:26
 * @Description:
 * @update:
 */
@Entity
@Table(name = "web_scan_warn", schema = "SDFS", catalog = "")
public class WebScanWarnEntity {
    private Long timeStamp;
    private String userName;
    private String sourceIp;
    private String destinationIp;
    private String sourcePort;
    private String destinationPort;
    private String loginPlace;
    private String loginSystem;
    private String loginMajor;
    private String requestType;
    private String url;
    private String pocName;

    @Basic
    @Column(name = "timeStamp")
    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Basic
    @Column(name = "userName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "sourceIp")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "destinationIp")
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "sourcePort")
    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Basic
    @Column(name = "destinationPort")
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "loginPlace")
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "loginSystem")
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "loginMajor")
    public String getLoginMajor() {
        return loginMajor;
    }

    public void setLoginMajor(String loginMajor) {
        this.loginMajor = loginMajor;
    }

    @Basic
    @Column(name = "requestType")
    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
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
    @Column(name = "pocName")
    public String getPocName() {
        return pocName;
    }

    public void setPocName(String pocName) {
        this.pocName = pocName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebScanWarnEntity that = (WebScanWarnEntity) o;
        return Objects.equals(timeStamp, that.timeStamp) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(sourcePort, that.sourcePort) &&
                Objects.equals(destinationPort, that.destinationPort) &&
                Objects.equals(loginPlace, that.loginPlace) &&
                Objects.equals(loginSystem, that.loginSystem) &&
                Objects.equals(loginMajor, that.loginMajor) &&
                Objects.equals(requestType, that.requestType) &&
                Objects.equals(url, that.url) &&
                Objects.equals(pocName, that.pocName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeStamp, userName, sourceIp, destinationIp, sourcePort, destinationPort, loginPlace, loginSystem, loginMajor, requestType, url, pocName);
    }
}
