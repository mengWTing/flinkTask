package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author hanyu
 * @ClassName PermeateSoftwareFlowWarnEntity
 * @date 2022/9/20 10:54
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "permeate_software_flow_warn", schema = "SDFS", catalog = "")
public class PermeateSoftwareFlowWarnEntity {
    private Timestamp alerttime;
    private String sourceip;
    private String desip;
    private String username;
    private String alertHost;
    private String alertUrl;
    private String alertCookie;
    private String alertXff;
    private String alertUseragent;
    private String alertAccept;
    private String alertType;

    @Basic
    @Column(name = "alerttime")
    public Timestamp getAlerttime() {
        return alerttime;
    }

    public void setAlerttime(Timestamp alerttime) {
        this.alerttime = alerttime;
    }

    @Basic
    @Column(name = "sourceip")
    public String getSourceip() {
        return sourceip;
    }

    public void setSourceip(String sourceip) {
        this.sourceip = sourceip;
    }

    @Basic
    @Column(name = "desip")
    public String getDesip() {
        return desip;
    }

    public void setDesip(String desip) {
        this.desip = desip;
    }

    @Basic
    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "alert_host")
    public String getAlertHost() {
        return alertHost;
    }

    public void setAlertHost(String alertHost) {
        this.alertHost = alertHost;
    }

    @Basic
    @Column(name = "alert_url")
    public String getAlertUrl() {
        return alertUrl;
    }

    public void setAlertUrl(String alertUrl) {
        this.alertUrl = alertUrl;
    }

    @Basic
    @Column(name = "alert_cookie")
    public String getAlertCookie() {
        return alertCookie;
    }

    public void setAlertCookie(String alertCookie) {
        this.alertCookie = alertCookie;
    }

    @Basic
    @Column(name = "alert_xff")
    public String getAlertXff() {
        return alertXff;
    }

    public void setAlertXff(String alertXff) {
        this.alertXff = alertXff;
    }

    @Basic
    @Column(name = "alert_useragent")
    public String getAlertUseragent() {
        return alertUseragent;
    }

    public void setAlertUseragent(String alertUseragent) {
        this.alertUseragent = alertUseragent;
    }

    @Basic
    @Column(name = "alert_accept")
    public String getAlertAccept() {
        return alertAccept;
    }

    public void setAlertAccept(String alertAccept) {
        this.alertAccept = alertAccept;
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
        PermeateSoftwareFlowWarnEntity that = (PermeateSoftwareFlowWarnEntity) o;
        return Objects.equals(alerttime, that.alerttime) && Objects.equals(sourceip, that.sourceip) && Objects.equals(desip, that.desip) && Objects.equals(username, that.username) && Objects.equals(alertHost, that.alertHost) && Objects.equals(alertUrl, that.alertUrl) && Objects.equals(alertCookie, that.alertCookie) && Objects.equals(alertXff, that.alertXff) && Objects.equals(alertUseragent, that.alertUseragent) && Objects.equals(alertAccept, that.alertAccept) && Objects.equals(alertType, that.alertType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alerttime, sourceip, desip, username, alertHost, alertUrl, alertCookie, alertXff, alertUseragent, alertAccept, alertType);
    }
}
