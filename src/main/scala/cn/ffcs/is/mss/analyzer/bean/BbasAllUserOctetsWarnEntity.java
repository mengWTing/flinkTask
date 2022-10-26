package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/6/26 14:40
 * @Modified By
 */
@Entity
@Table(name = "BBAS_ALL_USER_OCTETS_WARN", schema = "SDFS", catalog = "")
public class BbasAllUserOctetsWarnEntity {

    private Timestamp warnDatetime;
    private Long regressionValue;
    private Long realValue;
    private Integer warnLevel;

    @Id
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasAllUserOctetsWarnEntity that = (BbasAllUserOctetsWarnEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(regressionValue, that.regressionValue) &&
            Objects.equals(realValue, that.realValue) &&
            Objects.equals(warnLevel, that.warnLevel);
    }

    @Override
    public int hashCode() {

        return Objects.hash(warnDatetime, regressionValue, realValue, warnLevel);
    }
}
