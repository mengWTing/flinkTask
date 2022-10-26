package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/12/20 19:54
 * @Modified By
 */
public class BbasIpPortSourceRegressionConfigEntityPK implements Serializable {

    private String systemIp;
    private Integer destinationPort;
    private String regressionType;

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

    @Column(name = "REGRESSION_TYPE", nullable = false, length = 45)
    @Id
    public String getRegressionType() {
        return regressionType;
    }

    public void setRegressionType(String regressionType) {
        this.regressionType = regressionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasIpPortSourceRegressionConfigEntityPK that = (BbasIpPortSourceRegressionConfigEntityPK) o;

        if (systemIp != null ? !systemIp.equals(that.systemIp) : that.systemIp != null) {
            return false;
        }
        if (destinationPort != null ? !destinationPort.equals(that.destinationPort)
            : that.destinationPort != null) {
            return false;
        }
        if (regressionType != null ? !regressionType.equals(that.regressionType)
            : that.regressionType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = systemIp != null ? systemIp.hashCode() : 0;
        result = 31 * result + (destinationPort != null ? destinationPort.hashCode() : 0);
        result = 31 * result + (regressionType != null ? regressionType.hashCode() : 0);
        return result;
    }
}
