package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/13 16:12
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SYSTEM_PORT_DESTINATION_WARN_IP_CONFIG", schema = "SDFS", catalog = "")
public class BbasSystemPortDestinationWarnIpConfigEntity {

    private String systemIp;
    private Integer isolationForestT;
    private Double deltaProportion;
    private Integer ignoreConncount;

    @Id
    @Column(name = "SYSTEM_IP", nullable = false, length = 45)
    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
    }

    @Basic
    @Column(name = "ISOLATION_FOREST_T", nullable = false)
    public Integer getIsolationForestT() {
        return isolationForestT;
    }

    public void setIsolationForestT(Integer isolationForestT) {
        this.isolationForestT = isolationForestT;
    }

    @Basic
    @Column(name = "DELTA_PROPORTION", nullable = false, precision = 0)
    public Double getDeltaProportion() {
        return deltaProportion;
    }

    public void setDeltaProportion(Double deltaProportion) {
        this.deltaProportion = deltaProportion;
    }

    @Basic
    @Column(name = "IGNORE_CONNCOUNT", nullable = false)
    public Integer getIgnoreConncount() {
        return ignoreConncount;
    }

    public void setIgnoreConncount(Integer ignoreConncount) {
        this.ignoreConncount = ignoreConncount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasSystemPortDestinationWarnIpConfigEntity that = (BbasSystemPortDestinationWarnIpConfigEntity) o;

        if (systemIp != null ? !systemIp.equals(that.systemIp) : that.systemIp != null) {
            return false;
        }
        if (isolationForestT != null ? !isolationForestT.equals(that.isolationForestT)
            : that.isolationForestT != null) {
            return false;
        }
        if (deltaProportion != null ? !deltaProportion.equals(that.deltaProportion)
            : that.deltaProportion != null) {
            return false;
        }
        if (ignoreConncount != null ? !ignoreConncount.equals(that.ignoreConncount)
            : that.ignoreConncount != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = systemIp != null ? systemIp.hashCode() : 0;
        result = 31 * result + (isolationForestT != null ? isolationForestT.hashCode() : 0);
        result = 31 * result + (deltaProportion != null ? deltaProportion.hashCode() : 0);
        result = 31 * result + (ignoreConncount != null ? ignoreConncount.hashCode() : 0);
        return result;
    }
}
