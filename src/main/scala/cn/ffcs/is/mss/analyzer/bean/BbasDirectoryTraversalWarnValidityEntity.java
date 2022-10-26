package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @title BbasDirectoryTraversalWarnValidityEntity
 * @date 2021-03-24 09:18
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
@Entity
@Table(name = "BBAS_DIRECTORY_TRAVERSAL_WARN_VALIDITY", schema = "SDFS", catalog = "")
public class BbasDirectoryTraversalWarnValidityEntity {
    private Timestamp warnDatetime;
    private String username;
    private String loginSystem;
    private String destinationIp;
    private String loginPlace;
    private String sourceIp;
    private String url;
    private Integer httpStatus;
    private String packageTxt;
    private Integer validity;
    private String packageName;

    @Basic
    @Column(name = "WARN_DATETIME")
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Basic
    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
    @Column(name = "DESTINATION_IP")
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
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
    @Column(name = "SOURCE_IP")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "URL")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
    @Column(name = "PACKAGE_TXT")
    public String getPackageTxt() {
        return packageTxt;
    }

    public void setPackageTxt(String packageTxt) {
        this.packageTxt = packageTxt;
    }

    @Basic
    @Column(name = "VALIDITY")
    public Integer getValidity() {
        return validity;
    }

    public void setValidity(Integer validity) {
        this.validity = validity;
    }

    @Basic
    @Column(name = "PACKAGE_NAME")
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BbasDirectoryTraversalWarnValidityEntity that = (BbasDirectoryTraversalWarnValidityEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
                Objects.equals(username, that.username) &&
                Objects.equals(loginSystem, that.loginSystem) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(loginPlace, that.loginPlace) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(url, that.url) &&
                Objects.equals(httpStatus, that.httpStatus) &&
                Objects.equals(packageTxt, that.packageTxt) &&
                Objects.equals(validity, that.validity) &&
                Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, username, loginSystem, destinationIp, loginPlace, sourceIp, url, httpStatus, packageTxt, validity, packageName);
    }
}
