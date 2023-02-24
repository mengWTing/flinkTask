package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author hanyu
 * @ClassName ReboundShellWarnEntity
 * @date 2023/1/31 11:30
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "rebound_shell_warn", schema = "SDFS", catalog = "")
public class ReboundShellWarnEntity {
    private String sourceIp;
    private String shellIp;
    private String shellDesIp;
    private String shellInfo;
    private Timestamp alertTime;
    private String sourceIpPort;
    private String shellIpPort;
    private String shellDesIpPort;

    @Basic
    @Column(name = "source_ip")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "shell_ip")
    public String getShellIp() {
        return shellIp;
    }

    public void setShellIp(String shellIp) {
        this.shellIp = shellIp;
    }

    @Basic
    @Column(name = "shell_des_ip")
    public String getShellDesIp() {
        return shellDesIp;
    }

    public void setShellDesIp(String shellDesIp) {
        this.shellDesIp = shellDesIp;
    }

    @Basic
    @Column(name = "shell_info")
    public String getShellInfo() {
        return shellInfo;
    }

    public void setShellInfo(String shellInfo) {
        this.shellInfo = shellInfo;
    }

    @Basic
    @Column(name = "alert_time")
    public Timestamp getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Timestamp alertTime) {
        this.alertTime = alertTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReboundShellWarnEntity that = (ReboundShellWarnEntity) o;
        return Objects.equals(sourceIp, that.sourceIp) && Objects.equals(shellIp, that.shellIp) && Objects.equals(shellDesIp, that.shellDesIp) && Objects.equals(shellInfo, that.shellInfo) && Objects.equals(alertTime, that.alertTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceIp, shellIp, shellDesIp, shellInfo, alertTime);
    }

    @Basic
    @Column(name = "source_ip_port")
    public String getSourceIpPort() {
        return sourceIpPort;
    }

    public void setSourceIpPort(String sourceIpPort) {
        this.sourceIpPort = sourceIpPort;
    }

    @Basic
    @Column(name = "shell_ip_port")
    public String getShellIpPort() {
        return shellIpPort;
    }

    public void setShellIpPort(String shellIpPort) {
        this.shellIpPort = shellIpPort;
    }

    @Basic
    @Column(name = "shell_des_ip_port")
    public String getShellDesIpPort() {
        return shellDesIpPort;
    }

    public void setShellDesIpPort(String shellDesIpPort) {
        this.shellDesIpPort = shellDesIpPort;
    }
}
