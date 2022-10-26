package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

public class BbasSingleProvinceOctetsWarn1EntityPK implements Serializable {
    private Timestamp warnDatetime;
    private String provinceName;

    @Column(name = "WARN_DATETIME")
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "PROVINCE_NAME")
    @Id
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BbasSingleProvinceOctetsWarn1EntityPK that = (BbasSingleProvinceOctetsWarn1EntityPK) o;

        if (warnDatetime != null ? !warnDatetime.equals(that.warnDatetime) : that.warnDatetime != null) return false;
        if (provinceName != null ? !provinceName.equals(that.provinceName) : that.provinceName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnDatetime != null ? warnDatetime.hashCode() : 0;
        result = 31 * result + (provinceName != null ? provinceName.hashCode() : 0);
        return result;
    }
}
