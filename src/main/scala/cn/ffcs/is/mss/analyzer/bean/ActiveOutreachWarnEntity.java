package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @title ActiveOutreachWarnEntity
 * @date 2020-09-09 9:24
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
@Entity
@Table(name = "active_outreach_warn", schema = "SDFS", catalog = "")
public class ActiveOutreachWarnEntity {
    private String sourceIp;
    private String sourcePort;
    private String destinationIp;
    private String destinationPort;
    private Long outPutOctets;
    private Timestamp alertTime;

    @Basic
    @Column(name = "SOURCE_IP")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "SOURCE_PORT")
    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
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
    @Column(name = "DESTINATION_PORT")
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "OUT_PUT_OCTETS")
    public Long getOutPutOctets() {
        return outPutOctets;
    }

    public void setOutPutOctets(Long outPutOctets) {
        this.outPutOctets = outPutOctets;
    }

    @Basic
    @Column(name = "ALERT_TIME")
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
        ActiveOutreachWarnEntity that = (ActiveOutreachWarnEntity) o;
        return Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(sourcePort, that.sourcePort) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(destinationPort, that.destinationPort) &&
                Objects.equals(outPutOctets, that.outPutOctets) &&
                Objects.equals(alertTime, that.alertTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceIp, sourcePort, destinationIp, destinationPort, outPutOctets, alertTime);
    }
}
