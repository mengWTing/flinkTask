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
 * @Date: 2023/6/16 17:11
 * @Description:
 * @update:
 */
@Entity
@Table(name = "fscan_master_tcp", schema = "SDFS", catalog = "")
public class MasterTcpWarnEntity {
    private Timestamp alertTime;
    private String userName;
    private String sourceIp;
    private String sourcePorts;
    private String destinationIps;
    private String destinationPorts;
    private Long inputOctets;
    private Long outputOctets;
    protected Long scanCount;

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
    @Column(name = "source_ports")
    public String getSourcePorts() {
        return sourcePorts;
    }

    public void setSourcePorts(String sourcePorts) {
        this.sourcePorts = sourcePorts;
    }

    @Basic
    @Column(name = "destination_ips")
    public String getDestinationIps() {
        return destinationIps;
    }

    public void setDestinationIps(String destinationIps) {
        this.destinationIps = destinationIps;
    }

    @Basic
    @Column(name = "destination_ports")
    public String getDestinationPorts() {
        return destinationPorts;
    }

    public void setDestinationPorts(String destinationPorts) {
        this.destinationPorts = destinationPorts;
    }

    @Basic
    @Column(name = "input_octets")
    public Long getInputOctets() {
        return inputOctets;
    }

    public void setInputOctets(Long inputOctets) {
        this.inputOctets = inputOctets;
    }

    @Basic
    @Column(name = "output_octets")
    public Long getOutputOctets() {
        return outputOctets;
    }

    public void setOutputOctets(Long outputOctets) {
        this.outputOctets = outputOctets;
    }

    @Basic
    @Column(name = "scan_count")
    public Long getScanCount() {
        return scanCount;
    }

    public void setScanCount(Long scanCount) {
        this.scanCount = scanCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasterTcpWarnEntity that = (MasterTcpWarnEntity) o;
        return Objects.equals(alertTime, that.alertTime) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(sourcePorts, that.sourcePorts) &&
                Objects.equals(destinationIps, that.destinationIps) &&
                Objects.equals(destinationPorts, that.destinationPorts) &&
                Objects.equals(inputOctets, that.inputOctets) &&
                Objects.equals(outputOctets, that.outputOctets) &&
                Objects.equals(scanCount, that.scanCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertTime, userName, sourceIp, sourcePorts, destinationIps, destinationPorts, inputOctets, outputOctets, scanCount);
    }
}
