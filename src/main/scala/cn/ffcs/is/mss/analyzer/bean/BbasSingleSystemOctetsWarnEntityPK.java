package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/6/26 14:40
 * @Modified By
 */
public class BbasSingleSystemOctetsWarnEntityPK implements Serializable {

    private Timestamp warnDatetime;
    private String systemName;

    @Column(name = "WARN_DATETIME", nullable = false)
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "SYSTEM_NAME", nullable = false, length = 45)
    @Id
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasSingleSystemOctetsWarnEntityPK that = (BbasSingleSystemOctetsWarnEntityPK) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(systemName, that.systemName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(warnDatetime, systemName);
    }
}
