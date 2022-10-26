package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/12/25 17:11
 * @Modified By
 */
@Entity
@Table(name = "BBAS_IP_PORT_SOURCE_WARN", schema = "SDFS", catalog = "")
@IdClass(BbasIpPortSourceWarnEntityPK.class)
public class BbasIpPortSourceWarnEntity {

    private Timestamp warnDatetime;
    private String systemIp;
    private Integer destinationPort;
    private Long regressionValue;
    private Long realValue;
    private Integer warnLevel;
    private String systemName;
    private Long systemIpBinary;
    private Double deflection;

    @Id
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "SYSTEM_IP", nullable = false, length = 45)
    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
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
    @Column(name = "REGRESSION_VALUE", nullable = false)
    public Long getRegressionValue() {
        return regressionValue;
    }

    public void setRegressionValue(Long regressionValue) {
        this.regressionValue = regressionValue;
    }

    @Basic
    @Column(name = "REAL_VALUE", nullable = false)
    public Long getRealValue() {
        return realValue;
    }

    public void setRealValue(Long realValue) {
        this.realValue = realValue;
    }

    @Basic
    @Column(name = "WARN_LEVEL", nullable = false)
    public Integer getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(Integer warnLevel) {
        this.warnLevel = warnLevel;
    }

    @Basic
    @Column(name = "SYSTEM_NAME", nullable = false, length = 45)
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Basic
    @Column(name = "SYSTEM_IP_BINARY", nullable = false)
    public Long getSystemIpBinary() {
        return systemIpBinary;
    }

    public void setSystemIpBinary(Long systemIpBinary) {
        this.systemIpBinary = systemIpBinary;
    }

    @Basic
    @Column(name = "DEFLECTION", nullable = false, precision = 0)
    public Double getDeflection() {
        return deflection;
    }

    public void setDeflection(Double deflection) {
        this.deflection = deflection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasIpPortSourceWarnEntity that = (BbasIpPortSourceWarnEntity) o;

        if (warnDatetime != null ? !warnDatetime.equals(that.warnDatetime)
            : that.warnDatetime != null) {
            return false;
        }
        if (systemIp != null ? !systemIp.equals(that.systemIp) : that.systemIp != null) {
            return false;
        }
        if (destinationPort != null ? !destinationPort.equals(that.destinationPort)
            : that.destinationPort != null) {
            return false;
        }
        if (regressionValue != null ? !regressionValue.equals(that.regressionValue)
            : that.regressionValue != null) {
            return false;
        }
        if (realValue != null ? !realValue.equals(that.realValue) : that.realValue != null) {
            return false;
        }
        if (warnLevel != null ? !warnLevel.equals(that.warnLevel) : that.warnLevel != null) {
            return false;
        }
        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) {
            return false;
        }
        if (systemIpBinary != null ? !systemIpBinary.equals(that.systemIpBinary)
            : that.systemIpBinary != null) {
            return false;
        }
        if (deflection != null ? !deflection.equals(that.deflection) : that.deflection != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnDatetime != null ? warnDatetime.hashCode() : 0;
        result = 31 * result + (systemIp != null ? systemIp.hashCode() : 0);
        result = 31 * result + (destinationPort != null ? destinationPort.hashCode() : 0);
        result = 31 * result + (regressionValue != null ? regressionValue.hashCode() : 0);
        result = 31 * result + (realValue != null ? realValue.hashCode() : 0);
        result = 31 * result + (warnLevel != null ? warnLevel.hashCode() : 0);
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        result = 31 * result + (systemIpBinary != null ? systemIpBinary.hashCode() : 0);
        result = 31 * result + (deflection != null ? deflection.hashCode() : 0);
        return result;
    }
}
