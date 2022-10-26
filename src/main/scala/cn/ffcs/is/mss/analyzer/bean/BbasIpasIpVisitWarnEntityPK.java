package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/6/4 16:58
 * @Modified By
 */
public class BbasIpasIpVisitWarnEntityPK implements Serializable {

    private Timestamp warnDatetime;
    private String sourceIp;
    private String destinationIp;
    private Integer destinationPort;

    @Column(name = "WARN_DATETIME", nullable = false)
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "SOURCE_IP", nullable = false, length = 45)
    @Id
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Column(name = "DESTINATION_IP", nullable = false, length = 45)
    @Id
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Column(name = "DESTINATION_PORT", nullable = false)
    @Id
    public Integer getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasIpasIpVisitWarnEntityPK that = (BbasIpasIpVisitWarnEntityPK) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort);
    }

    @Override
    public int hashCode() {

        return Objects.hash(warnDatetime, sourceIp, destinationIp, destinationPort);
    }
}
