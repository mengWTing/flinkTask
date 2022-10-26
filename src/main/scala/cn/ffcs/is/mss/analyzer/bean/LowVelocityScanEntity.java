package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @title LowVelocityScanEntity
 * @date 2020-09-08 18:54
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
@Entity
@Table(name = "low_velocity_scan", schema = "SDFS", catalog = "")
public class LowVelocityScanEntity {
    private Integer lowScanType;
    private String sourceIp;
    private String destinationIp;
    private String destinationPort;
    private Long comentropy;
    private String protocolId;
    private String inputoctets;
    private Timestamp alertTime;

    @Basic
    @Column(name = "LOW_SCAN_TYPE")
    public Integer getLowScanType() {
        return lowScanType;
    }

    public void setLowScanType(Integer lowScanType) {
        this.lowScanType = lowScanType;
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
    @Column(name = "COMENTROPY")
    public Long getComentropy() {
        return comentropy;
    }

    public void setComentropy(Long comentropy) {
        this.comentropy = comentropy;
    }

    @Basic
    @Column(name = "PROTOCOL_ID")
    public String getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(String protocolId) {
        this.protocolId = protocolId;
    }

    @Basic
    @Column(name = "INPUTOCTETS")
    public String getInputoctets() {
        return inputoctets;
    }

    public void setInputoctets(String inputoctets) {
        this.inputoctets = inputoctets;
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
        LowVelocityScanEntity that = (LowVelocityScanEntity) o;
        return Objects.equals(lowScanType, that.lowScanType) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(destinationPort, that.destinationPort) &&
                Objects.equals(comentropy, that.comentropy) &&
                Objects.equals(protocolId, that.protocolId) &&
                Objects.equals(inputoctets, that.inputoctets) &&
                Objects.equals(alertTime, that.alertTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowScanType, sourceIp, destinationIp, destinationPort, comentropy, protocolId, inputoctets, alertTime);
    }
}
