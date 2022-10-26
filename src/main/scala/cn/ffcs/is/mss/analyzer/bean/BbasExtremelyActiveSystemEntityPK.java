package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/20 10:43
 * @Modified By
 */
public class BbasExtremelyActiveSystemEntityPK implements Serializable {

    private Timestamp activeDatetime;
    private String systemName;

    @Column(name = "ACTIVE_DATETIME", nullable = false)
    @Id
    public Timestamp getActiveDatetime() {
        return activeDatetime;
    }

    public void setActiveDatetime(Timestamp activeDatetime) {
        this.activeDatetime = activeDatetime;
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
        BbasExtremelyActiveSystemEntityPK that = (BbasExtremelyActiveSystemEntityPK) o;
        return Objects.equals(activeDatetime, that.activeDatetime) &&
            Objects.equals(systemName, that.systemName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeDatetime, systemName);
    }
}
