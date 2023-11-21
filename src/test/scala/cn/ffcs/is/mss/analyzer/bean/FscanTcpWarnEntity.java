package scala.cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/3/30 11:27
 * @Description:
 * @update:
 */
@Entity
@Table(name = "fscan_tcp_scan_warn", schema = "SDFS", catalog = "")
public class FscanTcpWarnEntity {
    private Long alertTime;
    private String srcIpPortDesIp;
    private Long scanCount;
    private String scanType;

    @Basic
    @Column(name = "alertStamp")
    public Long getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Long alertTime) {
        this.alertTime = alertTime;
    }

    @Basic
    @Column(name = "srcIpPortDesIp")
    public String getSrcIpPortDesIp() {
        return srcIpPortDesIp;
    }

    public void setSrcIpPortDesIp(String srcIpPortDesIp) {
        this.srcIpPortDesIp = srcIpPortDesIp;
    }

    @Basic
    @Column(name = "scanCount")
    public Long getScanCount() {
        return scanCount;
    }

    public void setScanCount(Long scanCount) {
        this.scanCount = scanCount;
    }

    @Basic
    @Column(name = "scanType")
    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FscanTcpWarnEntity that = (FscanTcpWarnEntity) o;
        return Objects.equals(alertTime, that.alertTime) &&
                Objects.equals(srcIpPortDesIp, that.srcIpPortDesIp) &&
                Objects.equals(scanCount, that.scanCount) &&
                Objects.equals(scanType, that.scanType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertTime, srcIpPortDesIp, scanCount, scanType);
    }
}
