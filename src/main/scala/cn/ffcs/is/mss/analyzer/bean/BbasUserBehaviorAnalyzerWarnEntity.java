/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-12 00:21:27
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author chenwei
 * @date 2019-12-12 00:21:27
 * @title BbasUserBehaviorAnalyzerWarnEntity
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
@Entity
@Table(name = "BBAS_USER_BEHAVIOR_ANALYZER_WARN", schema = "SDFS", catalog = "")
public class BbasUserBehaviorAnalyzerWarnEntity {

    private Integer id;
    private String userName;
    private Timestamp warnDatetime;
    private String sourceIp;
    private String place;
    private String destinationIp;
    private String destinationPort;
    private String host;
    private String system;
    private String url;
    private String httpStatus;
    private String userAgent;
    private String operationSystem;
    private String browser;
    private String cookie;
    private Long warnType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "USER_NAME", nullable = false, length = 45)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = false, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "PLACE", nullable = false, length = 45)
    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = false, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "DESTINATION_PORT", nullable = false, length = 45)
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "HOST", nullable = false, length = 200)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Basic
    @Column(name = "SYSTEM", nullable = false, length = 100)
    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    @Basic
    @Column(name = "URL", nullable = false, length = 2000)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "HTTP_STATUS", nullable = false, length = 10)
    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Basic
    @Column(name = "USER_AGENT", nullable = false, length = 500)
    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Basic
    @Column(name = "OPERATION_SYSTEM", nullable = false, length = 100)
    public String getOperationSystem() {
        return operationSystem;
    }

    public void setOperationSystem(String operationSystem) {
        this.operationSystem = operationSystem;
    }

    @Basic
    @Column(name = "BROWSER", nullable = false, length = 100)
    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    @Basic
    @Column(name = "COOKIE", nullable = false, length = 500)
    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    @Basic
    @Column(name = "WARN_TYPE", nullable = false)
    public Long getWarnType() {
        return warnType;
    }

    public void setWarnType(Long warnType) {
        this.warnType = warnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasUserBehaviorAnalyzerWarnEntity that = (BbasUserBehaviorAnalyzerWarnEntity) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(userName, that.userName) &&
            Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(place, that.place) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort) &&
            Objects.equals(host, that.host) &&
            Objects.equals(system, that.system) &&
            Objects.equals(url, that.url) &&
            Objects.equals(httpStatus, that.httpStatus) &&
            Objects.equals(userAgent, that.userAgent) &&
            Objects.equals(operationSystem, that.operationSystem) &&
            Objects.equals(browser, that.browser) &&
            Objects.equals(cookie, that.cookie) &&
            Objects.equals(warnType, that.warnType);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, userName, warnDatetime, sourceIp, place, destinationIp, destinationPort, host,
                system, url, httpStatus, userAgent, operationSystem, browser, cookie, warnType);
    }
}
