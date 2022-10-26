package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/12/20 18:59
 * @Modified By
 */
public class BbasIpDestinationRegressionConfigEntityPK implements Serializable {

    private String systemIp;
    private String regressionType;

    @Column(name = "SYSTEM_IP", nullable = false, length = 45)
    @Id
    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
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

        BbasIpDestinationRegressionConfigEntityPK that = (BbasIpDestinationRegressionConfigEntityPK) o;

        if (systemIp != null ? !systemIp.equals(that.systemIp) : that.systemIp != null) {
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
        result = 31 * result + (regressionType != null ? regressionType.hashCode() : 0);
        return result;
    }
}
