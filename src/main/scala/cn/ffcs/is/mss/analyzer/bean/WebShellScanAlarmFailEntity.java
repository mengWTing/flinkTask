package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "WEB_SHELL_SCAN_ALARM_FAIL", schema = "SDFS", catalog = "")
public class WebShellScanAlarmFailEntity {
    private String userName;
    private String sourceIp;
    private Timestamp attackTimeStart;
    private Timestamp attackTimeEnd;
    private Integer dangerLevel;
    private Long count;

    @Basic
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
    @Column(name = "ATTACK_TIME_START")
    public Timestamp getAttackTimeStart() {
        return attackTimeStart;
    }

    public void setAttackTimeStart(Timestamp attackTimeStart) {
        this.attackTimeStart = attackTimeStart;
    }

    @Basic
    @Column(name = "ATTACK_TIME_END")
    public Timestamp getAttackTimeEnd() {
        return attackTimeEnd;
    }

    public void setAttackTimeEnd(Timestamp attackTimeEnd) {
        this.attackTimeEnd = attackTimeEnd;
    }

    @Basic
    @Column(name = "DANGER_LEVEL")
    public Integer getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(Integer dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    @Basic
    @Column(name = "COUNT")
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebShellScanAlarmFailEntity that = (WebShellScanAlarmFailEntity) o;

        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) return false;
        if (attackTimeStart != null ? !attackTimeStart.equals(that.attackTimeStart) : that.attackTimeStart != null)
            return false;
        if (attackTimeEnd != null ? !attackTimeEnd.equals(that.attackTimeEnd) : that.attackTimeEnd != null)
            return false;
        if (dangerLevel != null ? !dangerLevel.equals(that.dangerLevel) : that.dangerLevel != null) return false;
        if (count != null ? !count.equals(that.count) : that.count != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (attackTimeStart != null ? attackTimeStart.hashCode() : 0);
        result = 31 * result + (attackTimeEnd != null ? attackTimeEnd.hashCode() : 0);
        result = 31 * result + (dangerLevel != null ? dangerLevel.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        return result;
    }
}
