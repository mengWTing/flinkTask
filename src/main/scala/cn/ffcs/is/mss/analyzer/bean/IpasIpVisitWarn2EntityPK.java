package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/25 09:30
 * @Modified By
 */
public class IpasIpVisitWarn2EntityPK implements Serializable {

    private Timestamp warnDate;
    private String sourceIp;
    private String sourcePort;
    private String destinationIp;
    private String destinationPort;

    @Column(name = "WARN_DATE", nullable = false)
    @Id
    public Timestamp getWarnDate() {
        return warnDate;
    }

    public void setWarnDate(Timestamp warnDate) {
        this.warnDate = warnDate;
    }

    @Column(name = "SOURCE_IP", nullable = false, length = 40)
    @Id
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Column(name = "SOURCE_PORT", nullable = false, length = 10)
    @Id
    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Column(name = "DESTINATION_IP", nullable = false, length = 40)
    @Id
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Column(name = "DESTINATION_PORT", nullable = false, length = 10)
    @Id
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
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
        IpasIpVisitWarn2EntityPK that = (IpasIpVisitWarn2EntityPK) o;
        return Objects.equals(warnDate, that.warnDate) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(sourcePort, that.sourcePort) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDate, sourceIp, sourcePort, destinationIp, destinationPort);
    }
}
