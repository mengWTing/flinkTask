package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/12/20 18:59
 * @Modified By
 */
@Entity
@Table(name = "BBAS_IP_DESTINATION_REGRESSION_CONFIG", schema = "SDFS", catalog = "")
@IdClass(BbasIpDestinationRegressionConfigEntityPK.class)
public class BbasIpDestinationRegressionConfigEntity {

    private String systemIp;
    private String regressionType;
    private Double regressionK;
    private String warnLevel;
    private Integer warnPoint;

    @Id
    @Column(name = "SYSTEM_IP", nullable = false, length = 45)
    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
    }

    @Id
    @Column(name = "REGRESSION_TYPE", nullable = false, length = 45)
    public String getRegressionType() {
        return regressionType;
    }

    public void setRegressionType(String regressionType) {
        this.regressionType = regressionType;
    }

    @Basic
    @Column(name = "REGRESSION_K", nullable = false, precision = 0)
    public Double getRegressionK() {
        return regressionK;
    }

    public void setRegressionK(Double regressionK) {
        this.regressionK = regressionK;
    }

    @Basic
    @Column(name = "WARN_LEVEL", nullable = false, length = 45)
    public String getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(String warnLevel) {
        this.warnLevel = warnLevel;
    }

    @Basic
    @Column(name = "WARN_POINT", nullable = false)
    public Integer getWarnPoint() {
        return warnPoint;
    }

    public void setWarnPoint(Integer warnPoint) {
        this.warnPoint = warnPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasIpDestinationRegressionConfigEntity that = (BbasIpDestinationRegressionConfigEntity) o;

        if (systemIp != null ? !systemIp.equals(that.systemIp) : that.systemIp != null) {
            return false;
        }
        if (regressionType != null ? !regressionType.equals(that.regressionType)
            : that.regressionType != null) {
            return false;
        }
        if (regressionK != null ? !regressionK.equals(that.regressionK)
            : that.regressionK != null) {
            return false;
        }
        if (warnLevel != null ? !warnLevel.equals(that.warnLevel) : that.warnLevel != null) {
            return false;
        }
        if (warnPoint != null ? !warnPoint.equals(that.warnPoint) : that.warnPoint != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = systemIp != null ? systemIp.hashCode() : 0;
        result = 31 * result + (regressionType != null ? regressionType.hashCode() : 0);
        result = 31 * result + (regressionK != null ? regressionK.hashCode() : 0);
        result = 31 * result + (warnLevel != null ? warnLevel.hashCode() : 0);
        result = 31 * result + (warnPoint != null ? warnPoint.hashCode() : 0);
        return result;
    }
}
