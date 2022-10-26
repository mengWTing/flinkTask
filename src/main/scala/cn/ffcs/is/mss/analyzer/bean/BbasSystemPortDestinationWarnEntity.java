package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/10 下午1:22
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SYSTEM_PORT_DESTINATION_WARN", schema = "SDFS", catalog = "")
public class BbasSystemPortDestinationWarnEntity {

    private Date warnDate;
    private String systemIp;
    private String systemName;
    private Integer systemPort;
    private Long historyConncount;
    private Long dayConncount;
    private Long ipCount;

    @Basic
    @Column(name = "WARN_DATE", nullable = false)
    public Date getWarnDate() {
        return warnDate;
    }

    public void setWarnDate(Date warnDate) {
        this.warnDate = warnDate;
    }

    @Basic
    @Column(name = "SYSTEM_IP", nullable = false, length = 45)
    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
    }

    @Basic
    @Column(name = "SYSTEM_NAME", nullable = false, length = 100)
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Basic
    @Column(name = "SYSTEM_PORT", nullable = false)
    public Integer getSystemPort() {
        return systemPort;
    }

    public void setSystemPort(Integer systemPort) {
        this.systemPort = systemPort;
    }

    @Basic
    @Column(name = "HISTORY_CONNCOUNT", nullable = false)
    public Long getHistoryConncount() {
        return historyConncount;
    }

    public void setHistoryConncount(Long historyConncount) {
        this.historyConncount = historyConncount;
    }

    @Basic
    @Column(name = "DAY_CONNCOUNT", nullable = false)
    public Long getDayConncount() {
        return dayConncount;
    }

    public void setDayConncount(Long dayConncount) {
        this.dayConncount = dayConncount;
    }

    @Basic
    @Column(name = "IP_COUNT", nullable = false)
    public Long getIpCount() {
        return ipCount;
    }

    public void setIpCount(Long ipCount) {
        this.ipCount = ipCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasSystemPortDestinationWarnEntity that = (BbasSystemPortDestinationWarnEntity) o;

        if (warnDate != null ? !warnDate.equals(that.warnDate) : that.warnDate != null) {
            return false;
        }
        if (systemIp != null ? !systemIp.equals(that.systemIp) : that.systemIp != null) {
            return false;
        }
        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) {
            return false;
        }
        if (systemPort != null ? !systemPort.equals(that.systemPort) : that.systemPort != null) {
            return false;
        }
        if (historyConncount != null ? !historyConncount.equals(that.historyConncount)
            : that.historyConncount != null) {
            return false;
        }
        if (dayConncount != null ? !dayConncount.equals(that.dayConncount)
            : that.dayConncount != null) {
            return false;
        }
        if (ipCount != null ? !ipCount.equals(that.ipCount) : that.ipCount != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnDate != null ? warnDate.hashCode() : 0;
        result = 31 * result + (systemIp != null ? systemIp.hashCode() : 0);
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        result = 31 * result + (systemPort != null ? systemPort.hashCode() : 0);
        result = 31 * result + (historyConncount != null ? historyConncount.hashCode() : 0);
        result = 31 * result + (dayConncount != null ? dayConncount.hashCode() : 0);
        result = 31 * result + (ipCount != null ? ipCount.hashCode() : 0);
        return result;
    }
}
