package cn.ffcs.is.mss.analyzer.bean;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/21 14:29
 * @Modified By
 */
@Entity
@Table(name = "alarm_rules", schema = "SDFS", catalog = "")
public class AlarmRulesEntity {

    private BigInteger id;
    private String alarmname;
    private String alarmlevel;
    private Long alarmtypes;
    private String alarmmark;
    private String alarmmeasures;
    private String srcip;
    private Integer srcconditions;
    private String destip;
    private Integer destconditions;
    private String ports;
    private Integer portconditions;
    private Long morenumber;
    private Long timelength;
    private Integer timeintensity;
    private Timestamp ctime;
    private Integer validTag;
    private Long createUserId;
    private Integer automatic;
    private Integer affected;
    private Integer statisticDimension;

    @Id
    @Column(name = "ID", nullable = false)
    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    @Basic
    @Column(name = "ALARMNAME", nullable = true, length = 255)
    public String getAlarmname() {
        return alarmname;
    }

    public void setAlarmname(String alarmname) {
        this.alarmname = alarmname;
    }

    @Basic
    @Column(name = "ALARMLEVEL", nullable = true, length = 2)
    public String getAlarmlevel() {
        return alarmlevel;
    }

    public void setAlarmlevel(String alarmlevel) {
        this.alarmlevel = alarmlevel;
    }

    @Basic
    @Column(name = "ALARMTYPES", nullable = true)
    public Long getAlarmtypes() {
        return alarmtypes;
    }

    public void setAlarmtypes(Long alarmtypes) {
        this.alarmtypes = alarmtypes;
    }

    @Basic
    @Column(name = "ALARMMARK", nullable = true, length = 2048)
    public String getAlarmmark() {
        return alarmmark;
    }

    public void setAlarmmark(String alarmmark) {
        this.alarmmark = alarmmark;
    }

    @Basic
    @Column(name = "ALARMMEASURES", nullable = true, length = 2048)
    public String getAlarmmeasures() {
        return alarmmeasures;
    }

    public void setAlarmmeasures(String alarmmeasures) {
        this.alarmmeasures = alarmmeasures;
    }

    @Basic
    @Column(name = "SRCIP", nullable = true, length = 2048)
    public String getSrcip() {
        return srcip;
    }

    public void setSrcip(String srcip) {
        this.srcip = srcip;
    }

    @Basic
    @Column(name = "SRCCONDITIONS", nullable = true)
    public Integer getSrcconditions() {
        return srcconditions;
    }

    public void setSrcconditions(Integer srcconditions) {
        this.srcconditions = srcconditions;
    }

    @Basic
    @Column(name = "DESTIP", nullable = true, length = 2048)
    public String getDestip() {
        return destip;
    }

    public void setDestip(String destip) {
        this.destip = destip;
    }

    @Basic
    @Column(name = "DESTCONDITIONS", nullable = true)
    public Integer getDestconditions() {
        return destconditions;
    }

    public void setDestconditions(Integer destconditions) {
        this.destconditions = destconditions;
    }

    @Basic
    @Column(name = "PORTS", nullable = true, length = 2048)
    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    @Basic
    @Column(name = "PORTCONDITIONS", nullable = true)
    public Integer getPortconditions() {
        return portconditions;
    }

    public void setPortconditions(Integer portconditions) {
        this.portconditions = portconditions;
    }

    @Basic
    @Column(name = "MORENUMBER", nullable = true)
    public Long getMorenumber() {
        return morenumber;
    }

    public void setMorenumber(Long morenumber) {
        this.morenumber = morenumber;
    }

    @Basic
    @Column(name = "TIMELENGTH", nullable = true)
    public Long getTimelength() {
        return timelength;
    }

    public void setTimelength(Long timelength) {
        this.timelength = timelength;
    }

    @Basic
    @Column(name = "TIMEINTENSITY", nullable = true)
    public Integer getTimeintensity() {
        return timeintensity;
    }

    public void setTimeintensity(Integer timeintensity) {
        this.timeintensity = timeintensity;
    }

    @Basic
    @Column(name = "CTIME", nullable = true)
    public Timestamp getCtime() {
        return ctime;
    }

    public void setCtime(Timestamp ctime) {
        this.ctime = ctime;
    }

    @Basic
    @Column(name = "VALID_TAG", nullable = true)
    public Integer getValidTag() {
        return validTag;
    }

    public void setValidTag(Integer validTag) {
        this.validTag = validTag;
    }

    @Basic
    @Column(name = "CREATE_USER_ID", nullable = true)
    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    @Basic
    @Column(name = "AUTOMATIC", nullable = true)
    public Integer getAutomatic() {
        return automatic;
    }

    public void setAutomatic(Integer automatic) {
        this.automatic = automatic;
    }

    @Basic
    @Column(name = "AFFECTED", nullable = true)
    public Integer getAffected() {
        return affected;
    }

    public void setAffected(Integer affected) {
        this.affected = affected;
    }

    @Basic
    @Column(name = "STATISTIC_DIMENSION", nullable = true)
    public Integer getStatisticDimension() {
        return statisticDimension;
    }

    public void setStatisticDimension(Integer statisticDimension) {
        this.statisticDimension = statisticDimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlarmRulesEntity that = (AlarmRulesEntity) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(alarmname, that.alarmname) &&
            Objects.equals(alarmlevel, that.alarmlevel) &&
            Objects.equals(alarmtypes, that.alarmtypes) &&
            Objects.equals(alarmmark, that.alarmmark) &&
            Objects.equals(alarmmeasures, that.alarmmeasures) &&
            Objects.equals(srcip, that.srcip) &&
            Objects.equals(srcconditions, that.srcconditions) &&
            Objects.equals(destip, that.destip) &&
            Objects.equals(destconditions, that.destconditions) &&
            Objects.equals(ports, that.ports) &&
            Objects.equals(portconditions, that.portconditions) &&
            Objects.equals(morenumber, that.morenumber) &&
            Objects.equals(timelength, that.timelength) &&
            Objects.equals(timeintensity, that.timeintensity) &&
            Objects.equals(ctime, that.ctime) &&
            Objects.equals(validTag, that.validTag) &&
            Objects.equals(createUserId, that.createUserId) &&
            Objects.equals(automatic, that.automatic) &&
            Objects.equals(affected, that.affected) &&
            Objects.equals(statisticDimension, that.statisticDimension);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, alarmname, alarmlevel, alarmtypes, alarmmark, alarmmeasures, srcip,
                srcconditions,
                destip, destconditions, ports, portconditions, morenumber, timelength,
                timeintensity,
                ctime, validTag, createUserId, automatic, affected, statisticDimension);
    }
}
