package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/12 下午9:04
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SYSTEM_IP", schema = "SDFS", catalog = "")
public class BbasSystemIpEntity {

    private String systemName;
    private String ip;
    private String majorName;

    @Basic
    @Column(name = "SYSTEM_NAME", nullable = false, length = 45)
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Basic
    @Column(name = "IP", nullable = false, length = 45)
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Basic
    @Column(name = "MAJOR_NAME", nullable = false, length = 45)
    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasSystemIpEntity that = (BbasSystemIpEntity) o;

        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) {
            return false;
        }
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) {
            return false;
        }
        if (majorName != null ? !majorName.equals(that.majorName) : that.majorName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = systemName != null ? systemName.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (majorName != null ? majorName.hashCode() : 0);
        return result;
    }
}
