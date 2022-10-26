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
 * @Date: Created in 2018/12/21 16:25
 * @Modified By
 */
@Entity
@Table(name = "BBAS_IPAS_IP_VISIT_ ALARM", schema = "SDFS", catalog = "")
@IdClass(BbasIpasIpVisitAlarmEntityPK.class)
public class BbasIpasIpVisitAlarmEntity {

    private Timestamp warnStartDatatime;
    private Timestamp warnEndDatatime;
    private String warnName;
    private String sourceIp;
    private String destinationIp;
    private String destinationPort;
    private Long count;

    @Id
    @Column(name = "WARN_START_DATATIME", nullable = false)
    public Timestamp getWarnStartDatatime() {
        return warnStartDatatime;
    }

    public void setWarnStartDatatime(Timestamp warnStartDatatime) {
        this.warnStartDatatime = warnStartDatatime;
    }

    @Id
    @Column(name = "WARN_END_DATATIME", nullable = false)
    public Timestamp getWarnEndDatatime() {
        return warnEndDatatime;
    }

    public void setWarnEndDatatime(Timestamp warnEndDatatime) {
        this.warnEndDatatime = warnEndDatatime;
    }

    @Id
    @Column(name = "WARN_NAME", nullable = false, length = 200)
    public String getWarnName() {
        return warnName;
    }

    public void setWarnName(String warnName) {
        this.warnName = warnName;
    }

    @Id
    @Column(name = "SOURCE_IP", nullable = false, length = 48)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Id
    @Column(name = "DESTINATION_IP", nullable = false, length = 48)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Id
    @Column(name = "DESTINATION_PORT", nullable = false, length = 48)
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "COUNT", nullable = true)
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasIpasIpVisitAlarmEntity that = (BbasIpasIpVisitAlarmEntity) o;
        return Objects.equals(warnStartDatatime, that.warnStartDatatime) &&
            Objects.equals(warnEndDatatime, that.warnEndDatatime) &&
            Objects.equals(warnName, that.warnName) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort) &&
            Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnStartDatatime, warnEndDatatime, warnName, sourceIp, destinationIp,
            destinationPort, count);
    }
}
