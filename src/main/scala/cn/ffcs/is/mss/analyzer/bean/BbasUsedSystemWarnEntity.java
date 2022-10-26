package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/6 下午4:53
 * @Modified By
 */
@Entity
@Table(name = "BBAS_USED_SYSTEM_WARN", schema = "SDFS", catalog = "")
public class BbasUsedSystemWarnEntity {

    private Timestamp warnDatetime;
    private String userName;
    private String loginSystem;
    private Double historyProportion;
    private Double dayProportion;
    private Long userCount;

    @Basic
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Basic
    @Column(name = "USER_NAME", nullable = false, length = 45)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "LOGIN_SYSTEM", nullable = false, length = 45)
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "HISTORY_PROPORTION", nullable = false, precision = 0)
    public Double getHistoryProportion() {
        return historyProportion;
    }

    public void setHistoryProportion(Double historyProportion) {
        this.historyProportion = historyProportion;
    }

    @Basic
    @Column(name = "DAY_PROPORTION", nullable = false, precision = 0)
    public Double getDayProportion() {
        return dayProportion;
    }

    public void setDayProportion(Double dayProportion) {
        this.dayProportion = dayProportion;
    }

    @Basic
    @Column(name = "USER_COUNT", nullable = false)
    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasUsedSystemWarnEntity that = (BbasUsedSystemWarnEntity) o;

        if (warnDatetime != null ? !warnDatetime.equals(that.warnDatetime)
            : that.warnDatetime != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }
        if (loginSystem != null ? !loginSystem.equals(that.loginSystem)
            : that.loginSystem != null) {
            return false;
        }
        if (historyProportion != null ? !historyProportion.equals(that.historyProportion)
            : that.historyProportion != null) {
            return false;
        }
        if (dayProportion != null ? !dayProportion.equals(that.dayProportion)
            : that.dayProportion != null) {
            return false;
        }
        if (userCount != null ? !userCount.equals(that.userCount) : that.userCount != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnDatetime != null ? warnDatetime.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (loginSystem != null ? loginSystem.hashCode() : 0);
        result = 31 * result + (historyProportion != null ? historyProportion.hashCode() : 0);
        result = 31 * result + (dayProportion != null ? dayProportion.hashCode() : 0);
        result = 31 * result + (userCount != null ? userCount.hashCode() : 0);
        return result;
    }
}
