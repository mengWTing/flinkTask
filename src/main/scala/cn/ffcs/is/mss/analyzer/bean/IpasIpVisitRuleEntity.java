package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/20 14:54
 * @Modified By
 */
@Entity
@Table(name = "IPAS_IP_VISIT_RULE", schema = "SDFS", catalog = "")
public class IpasIpVisitRuleEntity {

    private Integer ruleId;
    private String ruleName;
    private String warnName;
    private String riskType;
    private String granularity;
    private Integer visitCount;
    private String sourceIp;
    private String sourcePort;
    private String destinationIp;
    private String destinationPort;

    @Id
    @Column(name = "RULE_ID", nullable = false)
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

    @Basic
    @Column(name = "RISK_TYPE", nullable = true, length = 40)
    public String getRiskType() {
        return riskType;
    }

    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }

    @Basic
    @Column(name = "GRANULARITY", nullable = true, length = 5)
    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    @Basic
    @Column(name = "VISIT_COUNT", nullable = true)
    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = true, length = 20)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "SOURCE_PORT", nullable = true, length = 10)
    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = true, length = 20)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "DESTINATION_PORT", nullable = true, length = 10)
    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        IpasIpVisitRuleEntity that = (IpasIpVisitRuleEntity) object;

        if (ruleId != null ? !ruleId.equals(that.ruleId) : that.ruleId != null) {
            return false;
        }
        if (ruleName != null ? !ruleName.equals(that.ruleName) : that.ruleName != null) {
            return false;
        }
        if (warnName != null ? !warnName.equals(that.warnName) : that.warnName != null) {
            return false;
        }
        if (riskType != null ? !riskType.equals(that.riskType) : that.riskType != null) {
            return false;
        }
        if (granularity != null ? !granularity.equals(that.granularity)
            : that.granularity != null) {
            return false;
        }
        if (visitCount != null ? !visitCount.equals(that.visitCount) : that.visitCount != null) {
            return false;
        }
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) {
            return false;
        }
        if (sourcePort != null ? !sourcePort.equals(that.sourcePort) : that.sourcePort != null) {
            return false;
        }
        if (destinationIp != null ? !destinationIp.equals(that.destinationIp)
            : that.destinationIp != null) {
            return false;
        }
        if (destinationPort != null ? !destinationPort.equals(that.destinationPort)
            : that.destinationPort != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruleId != null ? ruleId.hashCode() : 0;
        result = 31 * result + (ruleName != null ? ruleName.hashCode() : 0);
        result = 31 * result + (warnName != null ? warnName.hashCode() : 0);
        result = 31 * result + (riskType != null ? riskType.hashCode() : 0);
        result = 31 * result + (granularity != null ? granularity.hashCode() : 0);
        result = 31 * result + (visitCount != null ? visitCount.hashCode() : 0);
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (sourcePort != null ? sourcePort.hashCode() : 0);
        result = 31 * result + (destinationIp != null ? destinationIp.hashCode() : 0);
        result = 31 * result + (destinationPort != null ? destinationPort.hashCode() : 0);
        return result;
    }
}
