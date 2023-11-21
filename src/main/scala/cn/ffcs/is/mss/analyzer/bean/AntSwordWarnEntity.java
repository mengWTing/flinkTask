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
 * @Date: 2023/5/10 16:31
 * @Description:
 * @update:
 */
@Entity
@Table(name = "ant_sword_warn", schema = "SDFS", catalog = "")
public class AntSwordWarnEntity {
    private Timestamp alertTime;
    private String userName;
    private String sourceIp;
    private String destinationIp;
    private String loginSystem;
    private String loginPlace;
    private Integer httpStatus;
    private String formValue;

    @Basic
    @Column(name = "alerttime")
    public Timestamp getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Timestamp alertTime) {
        this.alertTime = alertTime;
    }

    @Basic
    @Column(name = "username")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "loginsystem")
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "sourceip")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "destinationip")
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "loginplace")
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "httpstatus")
    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Basic
    @Column(name = "formvalue")
    public String getFormValue() {
        return formValue;
    }

    public void setFormValue(String formValue) {
        this.formValue = formValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AntSwordWarnEntity that = (AntSwordWarnEntity) o;
        return Objects.equals(alertTime, that.alertTime) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(loginSystem, that.loginSystem) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(loginPlace, that.loginPlace) &&
                Objects.equals(httpStatus, that.httpStatus) &&
                Objects.equals(formValue, that.formValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertTime, userName, loginSystem, sourceIp, destinationIp, loginPlace, httpStatus, formValue);
    }
}
