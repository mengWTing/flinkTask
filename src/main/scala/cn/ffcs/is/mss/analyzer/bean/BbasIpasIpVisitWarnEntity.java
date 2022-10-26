package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/6/4 16:58
 * @Modified By
 */
@Entity
@Table(name = "BBAS_IPAS_IP_VISIT_WARN", schema = "SDFS", catalog = "")
@IdClass(BbasIpasIpVisitWarnEntityPK.class)
public class BbasIpasIpVisitWarnEntity {

    private Timestamp warnDatetime;
    private String sourceIp;
    private String destinationIp;
    private Integer destinationPort;
    private String warnInfo;

    @Id
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "SOURCE_IP", nullable = false, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Id
    @Column(name = "DESTINATION_IP", nullable = false, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Id
    @Column(name = "DESTINATION_PORT", nullable = false)
    public Integer getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "WARN_INFO", nullable = false, length = 1000)
    public String getWarnInfo() {
        return warnInfo;
    }

    public void setWarnInfo(String warnInfo) {
        this.warnInfo = warnInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasIpasIpVisitWarnEntity that = (BbasIpasIpVisitWarnEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort) &&
            Objects.equals(warnInfo, that.warnInfo);
    }

    @Override
    public int hashCode() {

        return Objects.hash(warnDatetime, sourceIp, destinationIp, destinationPort, warnInfo);
    }

}
