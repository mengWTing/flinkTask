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
public class BbasExtremelyActiveUserEntityPK implements Serializable {

    private Timestamp activeDatetime;
    private String username;

    @Column(name = "ACTIVE_DATETIME", nullable = false)
    @Id
    public Timestamp getActiveDatetime() {
        return activeDatetime;
    }

    public void setActiveDatetime(Timestamp activeDatetime) {
        this.activeDatetime = activeDatetime;
    }

    @Column(name = "USERNAME", nullable = false, length = 45)
    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasExtremelyActiveUserEntityPK that = (BbasExtremelyActiveUserEntityPK) o;
        return Objects.equals(activeDatetime, that.activeDatetime) &&
            Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeDatetime, username);
    }
}
