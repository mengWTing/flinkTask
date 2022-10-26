package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/22 14:43
 * @Modified By
 */
public class BbasIpPortDestinationRegressionEntityPK implements Serializable {

    private Date regressionDate;
    private String systemIp;
    private Integer destinationPort;

    @Column(name = "REGRESSION_DATE", nullable = false)
    @Id
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
    }

    @Column(name = "SYSTEM_IP", nullable = false, length = 45)
    @Id
    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
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

        BbasIpPortDestinationRegressionEntityPK that = (BbasIpPortDestinationRegressionEntityPK) o;

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

        return true;
    }

    @Override
    public int hashCode() {
        int result = regressionDate != null ? regressionDate.hashCode() : 0;
        result = 31 * result + (systemIp != null ? systemIp.hashCode() : 0);
        result = 31 * result + (destinationPort != null ? destinationPort.hashCode() : 0);
        return result;
    }
}
