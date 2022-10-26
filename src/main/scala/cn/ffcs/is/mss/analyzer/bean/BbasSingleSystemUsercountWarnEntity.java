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
 * @Date: Created in 2018/6/26 14:40
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SINGLE_SYSTEM_USERCOUNT_WARN", schema = "SDFS", catalog = "")
@IdClass(BbasSingleSystemUsercountWarnEntityPK.class)
public class BbasSingleSystemUsercountWarnEntity {

    private Timestamp warnDatetime;
    private String systemName;
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

    @Id
    @Column(name = "SYSTEM_NAME", nullable = false, length = 45)
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
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
        BbasSingleSystemUsercountWarnEntity that = (BbasSingleSystemUsercountWarnEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(systemName, that.systemName) &&
            Objects.equals(regressionValue, that.regressionValue) &&
            Objects.equals(realValue, that.realValue) &&
            Objects.equals(warnLevel, that.warnLevel);
    }

    @Override
    public int hashCode() {

        return Objects.hash(warnDatetime, systemName, regressionValue, realValue, warnLevel);
    }
}
