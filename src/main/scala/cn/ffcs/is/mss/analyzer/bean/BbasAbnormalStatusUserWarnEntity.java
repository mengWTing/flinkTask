/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-04-04 10:23:15
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
 * @date 2019-04-04 10:23:15
 * @title BbasAbnormalStatusUserWarnEntity
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
@Entity
@Table(name = "BBAS_ABNORMAL_STATUS_USER_WARN", schema = "SDFS", catalog = "")
@IdClass(BbasAbnormalStatusUserWarnEntityPK.class)
public class BbasAbnormalStatusUserWarnEntity {

    private Timestamp warnDatetime;
    private String username;
    private String loginSystem;
    private String destinationIp;
    private String loginPlace;
    private String sourceIp;
    private String status;

    @Id
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Id
    @Column(name = "USERNAME", nullable = false, length = 45)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "LOGIN_SYSTEM", nullable = true, length = 45)
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = true, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "LOGIN_PLACE", nullable = true, length = 45)
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = true, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "STATUS", nullable = true, length = 45)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasAbnormalStatusUserWarnEntity that = (BbasAbnormalStatusUserWarnEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(username, that.username) &&
            Objects.equals(loginSystem, that.loginSystem) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(loginPlace, that.loginPlace) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(warnDatetime, username, loginSystem, destinationIp, loginPlace, sourceIp, status);
    }
}
