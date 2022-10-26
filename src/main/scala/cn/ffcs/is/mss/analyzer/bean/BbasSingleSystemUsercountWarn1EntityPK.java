package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

public class BbasSingleSystemUsercountWarn1EntityPK implements Serializable {
    private Timestamp warnDatetime;
    private String systemName;

    @Column(name = "WARN_DATETIME")
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "SYSTEM_NAME")
    @Id
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BbasSingleSystemUsercountWarn1EntityPK that = (BbasSingleSystemUsercountWarn1EntityPK) o;

        if (warnDatetime != null ? !warnDatetime.equals(that.warnDatetime) : that.warnDatetime != null) return false;
        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnDatetime != null ? warnDatetime.hashCode() : 0;
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        return result;
    }
}
