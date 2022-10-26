/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2020-03-30 18:17:34
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
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
 * @author chenwei
 * @date 2020-03-30 18:17:34
 * @title BbasSameTimeDifferentPlaceWarnEntity
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
@Entity
@Table(name = "BBAS_SAME_TIME_DIFFERENT_PLACE_WARN", schema = "SDFS", catalog = "")
@IdClass(BbasSameTimeDifferentPlaceWarnEntityPK.class)
public class BbasSameTimeDifferentPlaceWarnEntity {

    private Timestamp warnDatetime;
    private String userName;
    private String sourceIp;
    private String loginPlace;
    private String destinationIp;
    private String loginSystem;
    private String operation;
    private String lastLoginPlace;
    private Timestamp lastLoginDatetime;
    private String todayLoginPlace;
    private String url;

    @Id
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "USER_NAME", nullable = false, length = 45)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Id
    @Column(name = "SOURCE_IP", nullable = false, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "LOGIN_PLACE", nullable = false, length = 45)
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Id
    @Column(name = "DESTINATION_IP", nullable = false, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
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
    @Column(name = "OPERATION", nullable = false, length = 45)
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Basic
    @Column(name = "LAST_LOGIN_PLACE", nullable = false, length = 45)
    public String getLastLoginPlace() {
        return lastLoginPlace;
    }

    public void setLastLoginPlace(String lastLoginPlace) {
        this.lastLoginPlace = lastLoginPlace;
    }

    @Basic
    @Column(name = "LAST_LOGIN_DATETIME", nullable = false)
    public Timestamp getLastLoginDatetime() {
        return lastLoginDatetime;
    }

    public void setLastLoginDatetime(Timestamp lastLoginDatetime) {
        this.lastLoginDatetime = lastLoginDatetime;
    }

    @Basic
    @Column(name = "TODAY_LOGIN_PLACE", nullable = false, length = 300)
    public String getTodayLoginPlace() {
        return todayLoginPlace;
    }

    public void setTodayLoginPlace(String todayLoginPlace) {
        this.todayLoginPlace = todayLoginPlace;
    }

    @Basic
    @Column(name = "URL", nullable = true, length = 2000)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasSameTimeDifferentPlaceWarnEntity that = (BbasSameTimeDifferentPlaceWarnEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(userName, that.userName) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(loginPlace, that.loginPlace) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(loginSystem, that.loginSystem) &&
            Objects.equals(operation, that.operation) &&
            Objects.equals(lastLoginPlace, that.lastLoginPlace) &&
            Objects.equals(lastLoginDatetime, that.lastLoginDatetime) &&
            Objects.equals(todayLoginPlace, that.todayLoginPlace) &&
            Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(warnDatetime, userName, sourceIp, loginPlace, destinationIp, loginSystem,
                operation,
                lastLoginPlace, lastLoginDatetime, todayLoginPlace, url);
    }
}
