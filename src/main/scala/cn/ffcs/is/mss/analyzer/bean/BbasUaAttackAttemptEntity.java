package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @title BbasUaAttackAttemptEntity
 * @date 2021-03-24 14:26
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
@Entity
@Table(name = "BBAS_UA_ATTACK_ATTEMPT", schema = "SDFS", catalog = "")
public class BbasUaAttackAttemptEntity {
    private Timestamp warnDatetime;
    private String username;
    private String sourceIp;
    private String destinationIp;
    private String ua;
    private String uaAttackTypr;
    private String loginSystem;
    private String url;

    @Basic
    @Column(name = "WARN_DATETIME")
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Basic
    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "SOURCE_IP")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "DESTINATION_IP")
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "UA")
    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    @Basic
    @Column(name = "UA_ATTACK_TYPR")
    public String getUaAttackTypr() {
        return uaAttackTypr;
    }

    public void setUaAttackTypr(String uaAttackTypr) {
        this.uaAttackTypr = uaAttackTypr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BbasUaAttackAttemptEntity that = (BbasUaAttackAttemptEntity) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
                Objects.equals(username, that.username) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(ua, that.ua) &&
                Objects.equals(uaAttackTypr, that.uaAttackTypr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, username, sourceIp, destinationIp, ua, uaAttackTypr);
    }

    @Basic
    @Column(name = "LOGIN_SYSTEM")
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "URL")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
