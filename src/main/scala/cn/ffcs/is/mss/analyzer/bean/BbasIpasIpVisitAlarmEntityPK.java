package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/21 16:25
 * @Modified By
 */
public class BbasIpasIpVisitAlarmEntityPK implements Serializable {

    private Timestamp warnStartDatatime;
    private Timestamp warnEndDatatime;
    private String warnName;
    private String sourceIp;
    private String destinationIp;
    private String destinationPort;

    @Column(name = "WARN_START_DATATIME", nullable = false)
    @Id
    public Timestamp getWarnStartDatatime() {
        return warnStartDatatime;
    }

    public void setWarnStartDatatime(Timestamp warnStartDatatime) {
        this.warnStartDatatime = warnStartDatatime;
    }

    @Column(name = "WARN_END_DATATIME", nullable = false)
    @Id
    public Timestamp getWarnEndDatatime() {
        return warnEndDatatime;
    }

    public void setWarnEndDatatime(Timestamp warnEndDatatime) {
        this.warnEndDatatime = warnEndDatatime;
    }

    @Column(name = "WARN_NAME", nullable = false, length = 200)
    @Id
    public String getWarnName() {
        return warnName;
    }

    public void setWarnName(String warnName) {
        this.warnName = warnName;
    }

    @Column(name = "SOURCE_IP", nullable = false, length = 48)
    @Id
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Column(name = "DESTINATION_IP", nullable = false, length = 48)
    @Id
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Column(name = "DESTINATION_PORT", nullable = false, length = 48)
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
        BbasIpasIpVisitAlarmEntityPK that = (BbasIpasIpVisitAlarmEntityPK) o;
        return Objects.equals(warnStartDatatime, that.warnStartDatatime) &&
            Objects.equals(warnEndDatatime, that.warnEndDatatime) &&
            Objects.equals(warnName, that.warnName) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnStartDatatime, warnEndDatatime, warnName, sourceIp, destinationIp,
            destinationPort);
    }
}
