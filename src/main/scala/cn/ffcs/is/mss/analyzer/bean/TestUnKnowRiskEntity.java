package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/7/21 17:30
 * @Description:
 * @update:
 */
@Entity
@Table(name = "un_know_risk_test", schema = "SDFS", catalog = "")
    public class TestUnKnowRiskEntity {
    private long alertId;
    private String userName;
    private Timestamp alertTime;
    private String sourceIp;
    private String sourcePort;
    private String desIp;
    private String desPort;
    private String cmdInfo;
    private Integer isUnKnowRisk;
    private String riskName;
    private String loginSystem;
    private String loginMajor;
    private String loginPlace;
    private String isRemote;
    private String url;

    @Id
    @Column(name = "Alert_Id")
    public long getAlertId() {
        return alertId;
    }

    public void setAlertId(long alertId) {
        this.alertId = alertId;
    }

    @Basic
    @Column(name = "User_Name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "Alert_Time")
    public Timestamp getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Timestamp alertTime) {
        this.alertTime = alertTime;
    }

    @Basic
    @Column(name = "Source_IP")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "Source_Port")
    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Basic
    @Column(name = "Des_IP")
    public String getDesIp() {
        return desIp;
    }

    public void setDesIp(String desIp) {
        this.desIp = desIp;
    }

    @Basic
    @Column(name = "Des_Port")
    public String getDesPort() {
        return desPort;
    }

    public void setDesPort(String desPort) {
        this.desPort = desPort;
    }

    @Basic
    @Column(name = "CMD_Info")
    public String getCmdInfo() {
        return cmdInfo;
    }

    public void setCmdInfo(String cmdInfo) {
        this.cmdInfo = cmdInfo;
    }

    @Basic
    @Column(name = "Is_Un_Know_Risk")
    public Integer getIsUnKnowRisk() {
        return isUnKnowRisk;
    }

    public void setIsUnKnowRisk(Integer isUnKnowRisk) {
        this.isUnKnowRisk = isUnKnowRisk;
    }

    @Basic
    @Column(name = "Risk_Name")
    public String getRiskName() {
        return riskName;
    }

    public void setRiskName(String riskName) {
        this.riskName = riskName;
    }

    @Basic
    @Column(name = "Login_System")
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "Login_Major")
    public String getLoginMajor() {
        return loginMajor;
    }

    public void setLoginMajor(String loginMajor) {
        this.loginMajor = loginMajor;
    }

    @Basic
    @Column(name = "Login_Place")
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "IsRemote")
    public String getIsRemote() {
        return isRemote;
    }

    public void setIsRemote(String isRemote) {
        this.isRemote = isRemote;
    }

    @Basic
    @Column(name = "Url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUnKnowRiskEntity that = (TestUnKnowRiskEntity) o;
        return alertId == that.alertId && Objects.equals(userName, that.userName) && Objects.equals(alertTime, that.alertTime) && Objects.equals(sourceIp, that.sourceIp) && Objects.equals(sourcePort, that.sourcePort) && Objects.equals(desIp, that.desIp) && Objects.equals(desPort, that.desPort) && Objects.equals(cmdInfo, that.cmdInfo) && Objects.equals(isUnKnowRisk, that.isUnKnowRisk) && Objects.equals(riskName, that.riskName) && Objects.equals(loginSystem, that.loginSystem) && Objects.equals(loginMajor, that.loginMajor) && Objects.equals(loginPlace, that.loginPlace) && Objects.equals(isRemote, that.isRemote) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId, userName, alertTime, sourceIp, sourcePort, desIp, desPort, cmdInfo, isUnKnowRisk, riskName, loginSystem, loginMajor, loginPlace, isRemote, url);
    }
}
