package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/16 22:22
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SQL_INJECTION_WARN", schema = "SDFS", catalog = "")
public class BbasSqlInjectionWarnEntity {

    private Timestamp warnDatetime;
    private String username;
    private String loginSystem;
    private String destinationIp;
    private String loginPlace;
    private String sourceIp;
    private String url;
    private String formValue;
    private String injectionValue;
    private Integer httpStatus;

    @Basic
    @Column(name = "WARN_DATETIME", nullable = true)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Basic
    @Column(name = "USERNAME", nullable = true, length = 45)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "LOGIN_SYSTEM", nullable = true, length = 100)
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = true, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "LOGIN_PLACE", nullable = true, length = 45)
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = true, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "URL", nullable = true, length = 200)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "FORM_VALUE", nullable = true, length = 1000)
    public String getFormValue() {
        return formValue;
    }

    public void setFormValue(String formValue) {
        this.formValue = formValue;
    }

    @Basic
    @Column(name = "INJECTION_VALUE", nullable = true, length = 45)
    public String getInjectionValue() {
        return injectionValue;
    }

    public void setInjectionValue(String injectionValue) {
        this.injectionValue = injectionValue;
    }

    @Basic
    @Column(name="HTTP_STATUS",nullable = true)
    public Integer getHttpStatus() { return httpStatus; }

    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasSqlInjectionWarnEntity that = (BbasSqlInjectionWarnEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(username, that.username) &&
            Objects.equals(loginSystem, that.loginSystem) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(loginPlace, that.loginPlace) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(url, that.url) &&
            Objects.equals(formValue, that.formValue) &&
            Objects.equals(injectionValue, that.injectionValue) &&
            Objects.equals(httpStatus, that.httpStatus);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(warnDatetime, username, loginSystem, destinationIp, loginPlace, sourceIp, url,
                formValue, injectionValue, httpStatus);
    }
}
