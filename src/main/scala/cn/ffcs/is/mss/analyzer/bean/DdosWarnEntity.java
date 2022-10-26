package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "DDOS_WARN", schema = "SDFS", catalog = "")
public class DdosWarnEntity {
    private Long id;
    private String sourceIp;
    private String destIp;
    private Integer occurCount;
    private Timestamp warnTime;
    private Integer warnType;

    @Id
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = true, length = 255)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "DEST_IP", nullable = true, length = 255)
    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    @Basic
    @Column(name = "OCCUR_COUNT", nullable = true)
    public Integer getOccurCount() {
        return occurCount;
    }

    public void setOccurCount(Integer occurCount) {
        this.occurCount = occurCount;
    }

    @Basic
    @Column(name = "WARN_TIME", nullable = true)
    public Timestamp getWarnTime() {
        return warnTime;
    }

    public void setWarnTime(Timestamp warnTime) {
        this.warnTime = warnTime;
    }

    @Basic
    @Column(name = "WARN_TYPE", nullable = true)
    public Integer getWarnType() {
        return warnType;
    }

    public void setWarnType(Integer warnType) {
        this.warnType = warnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DdosWarnEntity that = (DdosWarnEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) return false;
        if (destIp != null ? !destIp.equals(that.destIp) : that.destIp != null) return false;
        if (occurCount != null ? !occurCount.equals(that.occurCount) : that.occurCount != null) return false;
        if (warnTime != null ? !warnTime.equals(that.warnTime) : that.warnTime != null) return false;
        if (warnType != null ? !warnType.equals(that.warnType) : that.warnType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (destIp != null ? destIp.hashCode() : 0);
        result = 31 * result + (occurCount != null ? occurCount.hashCode() : 0);
        result = 31 * result + (warnTime != null ? warnTime.hashCode() : 0);
        result = 31 * result + (warnType != null ? warnType.hashCode() : 0);
        return result;
    }
}
