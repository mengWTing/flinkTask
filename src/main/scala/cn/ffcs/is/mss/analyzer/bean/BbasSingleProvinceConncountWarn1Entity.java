package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "BBAS_SINGLE_PROVINCE_CONNCOUNT_WARN1", schema = "SDFS", catalog = "")
@IdClass(BbasSingleProvinceConncountWarn1EntityPK.class)
public class BbasSingleProvinceConncountWarn1Entity {
    private Timestamp warnDatetime;
    private String provinceName;
    private Long regressionValue;
    private Long realValue;
    private Integer warnLevel;

    @Id
    @Column(name = "WARN_DATETIME")
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "PROVINCE_NAME")
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Basic
    @Column(name = "REGRESSION_VALUE")
    public Long getRegressionValue() {
        return regressionValue;
    }

    public void setRegressionValue(Long regressionValue) {
        this.regressionValue = regressionValue;
    }

    @Basic
    @Column(name = "REAL_VALUE")
    public Long getRealValue() {
        return realValue;
    }

    public void setRealValue(Long realValue) {
        this.realValue = realValue;
    }

    @Basic
    @Column(name = "WARN_LEVEL")
    public Integer getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(Integer warnLevel) {
        this.warnLevel = warnLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BbasSingleProvinceConncountWarn1Entity that = (BbasSingleProvinceConncountWarn1Entity) o;

        if (warnDatetime != null ? !warnDatetime.equals(that.warnDatetime) : that.warnDatetime != null) return false;
        if (provinceName != null ? !provinceName.equals(that.provinceName) : that.provinceName != null) return false;
        if (regressionValue != null ? !regressionValue.equals(that.regressionValue) : that.regressionValue != null)
            return false;
        if (realValue != null ? !realValue.equals(that.realValue) : that.realValue != null) return false;
        if (warnLevel != null ? !warnLevel.equals(that.warnLevel) : that.warnLevel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnDatetime != null ? warnDatetime.hashCode() : 0;
        result = 31 * result + (provinceName != null ? provinceName.hashCode() : 0);
        result = 31 * result + (regressionValue != null ? regressionValue.hashCode() : 0);
        result = 31 * result + (realValue != null ? realValue.hashCode() : 0);
        result = 31 * result + (warnLevel != null ? warnLevel.hashCode() : 0);
        return result;
    }
}
