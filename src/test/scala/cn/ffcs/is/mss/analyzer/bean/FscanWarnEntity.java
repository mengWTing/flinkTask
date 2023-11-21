package scala.cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/3/3 10:57
 * @Description:
 * @update:
 */
@Entity
@Table(name = "fscan_warn", schema = "SDFS", catalog = "")
public class FscanWarnEntity {
    private Long alertTime;
    private String userName;
    private String sourceIp;
    private String destinationIp;
    private Long inputOctets;
    private Long outputOctets;
    private Long scanCount;

    @Basic
    @Column(name = "alerttime")
    public Long getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Long alertTime) {
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
    @Column(name = "inputoctets")
    public Long getInputOctets() {
        return inputOctets;
    }

    public void setInputOctets(Long inputOctets) {
        this.inputOctets = inputOctets;
    }

    @Basic
    @Column(name = "outputoctets")
    public Long getOutputOctets() {
        return outputOctets;
    }

    public void setOutputOctets(Long outputOctets) {
        this.outputOctets = outputOctets;
    }
    @Basic
    @Column(name = "scancount")
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
        FscanWarnEntity that = (FscanWarnEntity) o;
        return Objects.equals(alertTime, that.alertTime) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(inputOctets, that.inputOctets) &&
                Objects.equals(outputOctets, that.outputOctets) &&
                Objects.equals(scanCount, that.scanCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertTime, userName, sourceIp, destinationIp, inputOctets, outputOctets, scanCount);
    }
}
