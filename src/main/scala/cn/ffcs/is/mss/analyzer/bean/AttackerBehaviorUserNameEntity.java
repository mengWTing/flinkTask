package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @ClassName AttackerBehaviorUserNameEntity
 * @date 2022/3/18 10:46
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "attacker_behavior_user_name", schema = "SDFS", catalog = "")
public class AttackerBehaviorUserNameEntity {
    private Timestamp attackBehaviorTime;
    private String attackName;
    private String attackSourceIp;
    private String attackSourcePort;
    private String attackDesIp;
    private String attackDesPort;
    private Timestamp attackStartTime;
    private Timestamp attackStopTime;
    private String attackOrganization;
    private String attackLoginMajor;
    private String attackLoginSystem;
    private String attackUsedPlace;
    private String alertNameDetect;
    private String alertNameDeliver;
    private String alertNameInfiltrate;
    private String alertNameBreakThrough;
    private String alertNameControl;
    private String alertNameDestroy;

    @Basic
    @Column(name = "attack_behavior_time")
    public Timestamp getAttackBehaviorTime() {
        return attackBehaviorTime;
    }

    public void setAttackBehaviorTime(Timestamp attackBehaviorTime) {
        this.attackBehaviorTime = attackBehaviorTime;
    }

    @Basic
    @Column(name = "attack_name")
    public String getAttackName() {
        return attackName;
    }

    public void setAttackName(String attackName) {
        this.attackName = attackName;
    }

    @Basic
    @Column(name = "attack_source_ip")
    public String getAttackSourceIp() {
        return attackSourceIp;
    }

    public void setAttackSourceIp(String attackSourceIp) {
        this.attackSourceIp = attackSourceIp;
    }

    @Basic
    @Column(name = "attack_source_port")
    public String getAttackSourcePort() {
        return attackSourcePort;
    }

    public void setAttackSourcePort(String attackSourcePort) {
        this.attackSourcePort = attackSourcePort;
    }

    @Basic
    @Column(name = "attack_des_ip")
    public String getAttackDesIp() {
        return attackDesIp;
    }

    public void setAttackDesIp(String attackDesIp) {
        this.attackDesIp = attackDesIp;
    }

    @Basic
    @Column(name = "attack_des_port")
    public String getAttackDesPort() {
        return attackDesPort;
    }

    public void setAttackDesPort(String attackDesPort) {
        this.attackDesPort = attackDesPort;
    }

    @Basic
    @Column(name = "attack_start_time")
    public Timestamp getAttackStartTime() {
        return attackStartTime;
    }

    public void setAttackStartTime(Timestamp attackStartTime) {
        this.attackStartTime = attackStartTime;
    }

    @Basic
    @Column(name = "attack_stop_time")
    public Timestamp getAttackStopTime() {
        return attackStopTime;
    }

    public void setAttackStopTime(Timestamp attackStopTime) {
        this.attackStopTime = attackStopTime;
    }

    @Basic
    @Column(name = "attack_organization")
    public String getAttackOrganization() {
        return attackOrganization;
    }

    public void setAttackOrganization(String attackOrganization) {
        this.attackOrganization = attackOrganization;
    }

    @Basic
    @Column(name = "attack_loginMajor")
    public String getAttackLoginMajor() {
        return attackLoginMajor;
    }

    public void setAttackLoginMajor(String attackLoginMajor) {
        this.attackLoginMajor = attackLoginMajor;
    }

    @Basic
    @Column(name = "attack_loginSystem")
    public String getAttackLoginSystem() {
        return attackLoginSystem;
    }

    public void setAttackLoginSystem(String attackLoginSystem) {
        this.attackLoginSystem = attackLoginSystem;
    }

    @Basic
    @Column(name = "attack_usedPlace")
    public String getAttackUsedPlace() {
        return attackUsedPlace;
    }

    public void setAttackUsedPlace(String attackUsedPlace) {
        this.attackUsedPlace = attackUsedPlace;
    }

    @Basic
    @Column(name = "alert_name_detect")
    public String getAlertNameDetect() {
        return alertNameDetect;
    }

    public void setAlertNameDetect(String alertNameDetect) {
        this.alertNameDetect = alertNameDetect;
    }

    @Basic
    @Column(name = "alert_name_deliver")
    public String getAlertNameDeliver() {
        return alertNameDeliver;
    }

    public void setAlertNameDeliver(String alertNameDeliver) {
        this.alertNameDeliver = alertNameDeliver;
    }

    @Basic
    @Column(name = "alert_name_infiltrate")
    public String getAlertNameInfiltrate() {
        return alertNameInfiltrate;
    }

    public void setAlertNameInfiltrate(String alertNameInfiltrate) {
        this.alertNameInfiltrate = alertNameInfiltrate;
    }

    @Basic
    @Column(name = "alert_name_break_through")
    public String getAlertNameBreakThrough() {
        return alertNameBreakThrough;
    }

    public void setAlertNameBreakThrough(String alertNameBreakThrough) {
        this.alertNameBreakThrough = alertNameBreakThrough;
    }

    @Basic
    @Column(name = "alert_name_control")
    public String getAlertNameControl() {
        return alertNameControl;
    }

    public void setAlertNameControl(String alertNameControl) {
        this.alertNameControl = alertNameControl;
    }

    @Basic
    @Column(name = "alert_name_destroy")
    public String getAlertNameDestroy() {
        return alertNameDestroy;
    }

    public void setAlertNameDestroy(String alertNameDestroy) {
        this.alertNameDestroy = alertNameDestroy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttackerBehaviorUserNameEntity that = (AttackerBehaviorUserNameEntity) o;
        return Objects.equals(attackBehaviorTime, that.attackBehaviorTime) && Objects.equals(attackName, that.attackName) && Objects.equals(attackSourceIp, that.attackSourceIp) && Objects.equals(attackSourcePort, that.attackSourcePort) && Objects.equals(attackDesIp, that.attackDesIp) && Objects.equals(attackDesPort, that.attackDesPort) && Objects.equals(attackStartTime, that.attackStartTime) && Objects.equals(attackStopTime, that.attackStopTime) && Objects.equals(attackOrganization, that.attackOrganization) && Objects.equals(attackLoginMajor, that.attackLoginMajor) && Objects.equals(attackLoginSystem, that.attackLoginSystem) && Objects.equals(attackUsedPlace, that.attackUsedPlace) && Objects.equals(alertNameDetect, that.alertNameDetect) && Objects.equals(alertNameDeliver, that.alertNameDeliver) && Objects.equals(alertNameInfiltrate, that.alertNameInfiltrate) && Objects.equals(alertNameBreakThrough, that.alertNameBreakThrough) && Objects.equals(alertNameControl, that.alertNameControl) && Objects.equals(alertNameDestroy, that.alertNameDestroy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attackBehaviorTime, attackName, attackSourceIp, attackSourcePort, attackDesIp, attackDesPort, attackStartTime, attackStopTime, attackOrganization, attackLoginMajor, attackLoginSystem, attackUsedPlace, alertNameDetect, alertNameDeliver, alertNameInfiltrate, alertNameBreakThrough, alertNameControl, alertNameDestroy);
    }
}
