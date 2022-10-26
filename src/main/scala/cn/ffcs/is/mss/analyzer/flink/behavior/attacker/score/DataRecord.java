package cn.ffcs.is.mss.analyzer.flink.behavior.attacker.score;

/**
 * @author hanyu
 * @ClassName DataRecord
 * @date 2022/3/21 16:10
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class DataRecord {
    private String attackerName;

    private String attackSourceIp;
    private String attackSourcePort;
    private String attackDesIp;
    private String attackDesPort;

    private Long attackStartTime;
    private Long attackStopTime;

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

    public DataRecord() {

    }

    public DataRecord(String[] initStr) {
        if (initStr.length == 17)
            try {
                setAttackerName(initStr[0]);

                setAttackSourceIp(initStr[1]);
                setAttackSourcePort(initStr[2]);
                setAttackDesIp(initStr[3]);
                setAttackDesPort(initStr[4]);

                setAttackStartTime(initStr[5]);
                setAttackStopTime(initStr[6]);

                setAttackOrganization(initStr[7]);
                setAttackUsedPlace(initStr[8]);

                setAttackLoginMajor(initStr[9]);
                setAttackLoginSystem(initStr[10]);

                setAlertNameDetect(initStr[11]);
                setAlertNameDeliver(initStr[12]);
                setAlertNameInfiltrate(initStr[13]);
                setAlertNameBreakThrough(initStr[14]);
                setAlertNameControl(initStr[15]);
                setAlertNameDestroy(initStr[16]);


            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public String getAttackerName() {
        return attackerName;
    }

    public void setAttackerName(String attackName) {
        this.attackerName = attackerName;
    }

    public String getAttackSourceIp() {
        return attackSourceIp;
    }

    public void setAttackSourceIp(String attackSourceIp) {
        this.attackSourceIp = attackSourceIp;
    }

    public String getAttackSourcePort() {
        return attackSourcePort;
    }

    public void setAttackSourcePort(String attackSourcePort) {
        this.attackSourcePort = attackSourcePort;
    }

    public String getAttackDesIp() {
        return attackDesIp;
    }

    public void setAttackDesIp(String attackDesIp) {
        this.attackDesIp = attackDesIp;
    }

    public String getAttackDesPort() {
        return attackDesPort;
    }

    public void setAttackDesPort(String attackDesPort) {
        this.attackDesPort = attackDesPort;
    }

    public Long getAttackStartTime() {
        return attackStartTime;
    }

    public void setAttackStartTime(String attackStartTime) {

        this.attackStartTime = attackStartTime.isEmpty() ? 0 : Long.parseLong(attackStartTime);


    }

    public Long getAttackStopTime() {
        return attackStopTime;
    }

    public void setAttackStopTime(String attackStopTime) {
        this.attackStopTime = attackStopTime.isEmpty() ? 0 : Long.parseLong(attackStopTime);
    }

    public String getAttackOrganization() {
        return attackOrganization;
    }

    public void setAttackOrganization(String attackOrganization) {
        this.attackOrganization = attackOrganization;
    }

    public String getAttackLoginMajor() {
        return attackLoginMajor;
    }

    public void setAttackLoginMajor(String attackLoginMajor) {
        this.attackLoginMajor = attackLoginMajor;
    }

    public String getAttackLoginSystem() {
        return attackLoginSystem;
    }

    public void setAttackLoginSystem(String attackLoginSystem) {
        this.attackLoginSystem = attackLoginSystem;
    }

    public String getAttackUsedPlace() {
        return attackUsedPlace;
    }

    public void setAttackUsedPlace(String attackUsedPlace) {
        this.attackUsedPlace = attackUsedPlace;
    }

    public String getAlertNameDetect() {
        return alertNameDetect;
    }

    public void setAlertNameDetect(String alertNameDetect) {
        this.alertNameDetect = alertNameDetect;
    }

    public String getAlertNameDeliver() {
        return alertNameDeliver;
    }

    public void setAlertNameDeliver(String alertNameDeliver) {
        this.alertNameDeliver = alertNameDeliver;
    }

    public String getAlertNameInfiltrate() {
        return alertNameInfiltrate;
    }

    public void setAlertNameInfiltrate(String alertNameInfiltrate) {
        this.alertNameInfiltrate = alertNameInfiltrate;
    }

    public String getAlertNameBreakThrough() {
        return alertNameBreakThrough;
    }

    public void setAlertNameBreakThrough(String alertNameBreakThrough) {
        this.alertNameBreakThrough = alertNameBreakThrough;
    }

    public String getAlertNameControl() {
        return alertNameControl;
    }

    public void setAlertNameControl(String alertNameControl) {
        this.alertNameControl = alertNameControl;
    }

    public String getAlertNameDestroy() {
        return alertNameDestroy;
    }

    public void setAlertNameDestroy(String alertNameDestroy) {
        this.alertNameDestroy = alertNameDestroy;
    }

    @Override
    public String toString() {
        return "DataRecord{" +
                "attackerName='" + attackerName + '\'' +
                ", attackSourceIp='" + attackSourceIp + '\'' +
                ", attackSourcePort='" + attackSourcePort + '\'' +
                ", attackDesIp='" + attackDesIp + '\'' +
                ", attackDesPort='" + attackDesPort + '\'' +
                ", attackStartTime=" + attackStartTime +
                ", attackStopTime=" + attackStopTime +
                ", attackOrganization='" + attackOrganization + '\'' +
                ", attackLoginMajor='" + attackLoginMajor + '\'' +
                ", attackLoginSystem='" + attackLoginSystem + '\'' +
                ", attackUsedPlace='" + attackUsedPlace + '\'' +
                ", alertNameDetect='" + alertNameDetect + '\'' +
                ", alertNameDeliver='" + alertNameDeliver + '\'' +
                ", alertNameInfiltrate='" + alertNameInfiltrate + '\'' +
                ", alertNameBreakThrough='" + alertNameBreakThrough + '\'' +
                ", alertNameControl='" + alertNameControl + '\'' +
                ", alertNameDestroy='" + alertNameDestroy + '\'' +
                '}';
    }

}
