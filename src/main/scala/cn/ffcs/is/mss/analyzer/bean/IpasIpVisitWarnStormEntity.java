package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/11/23 17:42
 * @Modified By
 */
@Entity
@Table(name = "IPAS_IP_VISIT_WARN_STORM", schema = "SDFS", catalog = "")
@IdClass(IpasIpVisitWarnEntityPK.class)
public class IpasIpVisitWarnStormEntity {

    private Integer ruleId;
    private String ruleName;
    private String warnName;
    private Timestamp warnDate;
    private String granularity;
    public String sourceIp;
    private String sourcePort;
    private String destinationIp;
    private String destinationPort;
    private Integer visitCount;

    @Basic
    @Column(name = "RULE_ID", nullable = true)
    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    @Basic
    @Column(name = "RULE_NAME", nullable = true, length = 40)
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    @Basic
    @Column(name = "WARN_NAME", nullable = true, length = 40)
    public String getWarnName() {
        return warnName;
    }

    public void setWarnName(String warnName) {
        this.warnName = warnName;
    }

    @Id
    @Column(name = "WARN_DATE", nullable = false)
    public Timestamp getWarnDate() {
        return warnDate;
    }

    public void setWarnDate(Timestamp warnDate) {
        this.warnDate = warnDate;
    }

    @Basic
    @Column(name = "GRANULARITY", nullable = true, length = 5)
    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    @Id
    @Column(name = "SOURCE_IP", nullable = false, length = 40)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Id
    @Column(name = "SOURCE_PORT", nullable = false, length = 10)
    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Id
    @Column(name = "DESTINATION_IP", nullable = false, length = 40)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Id
    @Column(name = "DESTINATION_PORT", nullable = false, length = 10)
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Basic
    @Column(name = "VISIT_COUNT", nullable = true)
    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IpasIpVisitWarnStormEntity that = (IpasIpVisitWarnStormEntity) o;
        return Objects.equals(ruleId, that.ruleId) &&
            Objects.equals(ruleName, that.ruleName) &&
            Objects.equals(warnName, that.warnName) &&
            Objects.equals(warnDate, that.warnDate) &&
            Objects.equals(granularity, that.granularity) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(sourcePort, that.sourcePort) &&
            Objects.equals(destinationIp, that.destinationIp) &&
            Objects.equals(destinationPort, that.destinationPort) &&
            Objects.equals(visitCount, that.visitCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, ruleName, warnName, warnDate, granularity, sourceIp, sourcePort,
            destinationIp, destinationPort, visitCount);
    }
}
