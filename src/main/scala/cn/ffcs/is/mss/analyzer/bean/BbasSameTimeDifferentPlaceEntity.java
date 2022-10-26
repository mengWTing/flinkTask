package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/23 下午7:51
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SAME_TIME_DIFFERENT_PLACE", schema = "SDFS", catalog = "")
public class BbasSameTimeDifferentPlaceEntity {

    private Timestamp loginDatetime;
    private String userName;
    private String loginPlace;
    private String todayLoginPlace;

    @Basic
    @Column(name = "LOGIN_DATETIME", nullable = false)
    public Timestamp getLoginDatetime() {
        return loginDatetime;
    }

    public void setLoginDatetime(Timestamp loginDatetime) {
        this.loginDatetime = loginDatetime;
    }

    @Id
    @Column(name = "USER_NAME", nullable = false, length = 45)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "LOGIN_PLACE", nullable = false, length = 45)
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "TODAY_LOGIN_PLACE", nullable = false, length = 300)
    public String getTodayLoginPlace() {
        return todayLoginPlace;
    }

    public void setTodayLoginPlace(String todayLoginPlace) {
        this.todayLoginPlace = todayLoginPlace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasSameTimeDifferentPlaceEntity that = (BbasSameTimeDifferentPlaceEntity) o;

        if (loginDatetime != null ? !loginDatetime.equals(that.loginDatetime)
            : that.loginDatetime != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }
        if (loginPlace != null ? !loginPlace.equals(that.loginPlace) : that.loginPlace != null) {
            return false;
        }
        if (todayLoginPlace != null ? !todayLoginPlace.equals(that.todayLoginPlace)
            : that.todayLoginPlace != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = loginDatetime != null ? loginDatetime.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (loginPlace != null ? loginPlace.hashCode() : 0);
        result = 31 * result + (todayLoginPlace != null ? todayLoginPlace.hashCode() : 0);
        return result;
    }
}
