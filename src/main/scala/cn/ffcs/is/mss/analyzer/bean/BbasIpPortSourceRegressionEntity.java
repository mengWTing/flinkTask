package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/22 14:43
 * @Modified By
 */
@Entity
@Table(name = "BBAS_IP_PORT_SOURCE_REGRESSION", schema = "SDFS", catalog = "")
@IdClass(BbasIpPortSourceRegressionEntityPK.class)
public class BbasIpPortSourceRegressionEntity {

    private Date regressionDate;
    private String systemIp;
    private Integer destinationPort;
    private String regressionValueText;

    @Id
    @Column(name = "REGRESSION_DATE", nullable = false)
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
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
    @Column(name = "REGRESSION_VALUE_TEXT", nullable = false, length = -1)
    public String getRegressionValueText() {
        return regressionValueText;
    }

    public void setRegressionValueText(String regressionValueText) {
        this.regressionValueText = regressionValueText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasIpPortSourceRegressionEntity that = (BbasIpPortSourceRegressionEntity) o;

        if (regressionDate != null ? !regressionDate.equals(that.regressionDate)
            : that.regressionDate != null) {
            return false;
        }
        if (systemIp != null ? !systemIp.equals(that.systemIp) : that.systemIp != null) {
            return false;
        }
        if (destinationPort != null ? !destinationPort.equals(that.destinationPort)
            : that.destinationPort != null) {
            return false;
        }
        if (regressionValueText != null ? !regressionValueText.equals(that.regressionValueText)
            : that.regressionValueText != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = regressionDate != null ? regressionDate.hashCode() : 0;
        result = 31 * result + (systemIp != null ? systemIp.hashCode() : 0);
        result = 31 * result + (destinationPort != null ? destinationPort.hashCode() : 0);
        result = 31 * result + (regressionValueText != null ? regressionValueText.hashCode() : 0);
        return result;
    }
}
