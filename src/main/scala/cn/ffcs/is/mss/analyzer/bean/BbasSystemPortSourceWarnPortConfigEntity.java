package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/13 16:32
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SYSTEM_PORT_SOURCE_WARN_PORT_CONFIG", schema = "SDFS", catalog = "")
public class BbasSystemPortSourceWarnPortConfigEntity {

    private Integer systemPort;
    private Double deltaProportion;
    private Integer ignoreConncount;

    @Id
    @Column(name = "SYSTEM_PORT", nullable = false)
    public Integer getSystemPort() {
        return systemPort;
    }

    public void setSystemPort(Integer systemPort) {
        this.systemPort = systemPort;
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

        BbasSystemPortSourceWarnPortConfigEntity that = (BbasSystemPortSourceWarnPortConfigEntity) o;

        if (systemPort != null ? !systemPort.equals(that.systemPort) : that.systemPort != null) {
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
        int result = systemPort != null ? systemPort.hashCode() : 0;
        result = 31 * result + (deltaProportion != null ? deltaProportion.hashCode() : 0);
        result = 31 * result + (ignoreConncount != null ? ignoreConncount.hashCode() : 0);
        return result;
    }
}
