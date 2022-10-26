package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/24 14:45
 * @Modified By
 */
@Entity
@Table(name = "IP_VISIT_WARN_MERGE_STORM", schema = "SDFS", catalog = "")
public class IpVisitWarnMergeStormEntity {

    private Timestamp warnDatetime;
    private String sourceIp;
    private String destinationIp;
    private String destinationPort;
    private String warnName;
    private String info;

    @Basic
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = false, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = false, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "DESTINATION_PORT", nullable = false, length = 45)
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "WARN_NAME", nullable = false, length = 45)
    public String getWarnName() {
        return warnName;
    }

    public void setWarnName(String warnName) {
        this.warnName = warnName;
    }

    @Basic
    @Column(name = "INFO", nullable = false, length = 45)
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IpVisitWarnMergeStormEntity that = (IpVisitWarnMergeStormEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort) &&
            Objects.equals(warnName, that.warnName) &&
            Objects.equals(info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, sourceIp, destinationIp, destinationPort, warnName, info);
    }
}
