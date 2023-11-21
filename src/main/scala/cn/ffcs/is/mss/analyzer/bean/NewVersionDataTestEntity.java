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
 * @Date: 2023/6/28 17:57
 * @Description:
 * @update:
 */

@Entity
@Table(name = "new_version_data_test", schema = "SDFS", catalog = "")
public class NewVersionDataTestEntity {
    private Timestamp alertTime;
    private String userName;
    private String sourceIp;
    private String sourcePort;
    private String destinationIp;
    private String destinationPort;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewVersionDataTestEntity that = (NewVersionDataTestEntity) o;
        return Objects.equals(alertTime, that.alertTime) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(sourcePort, that.sourcePort) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(destinationPort, that.destinationPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertTime, userName, sourceIp, sourcePort, destinationIp, destinationPort);
    }
}
