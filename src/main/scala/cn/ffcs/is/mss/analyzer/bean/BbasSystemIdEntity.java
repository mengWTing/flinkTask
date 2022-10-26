package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/12/12 14:11
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SYSTEM_ID", schema = "SDFS", catalog = "")
public class BbasSystemIdEntity {

    private Integer systemId;
    private String systemName;
    private String majorName;

    @Basic
    @Column(name = "SYSTEM_ID", nullable = false)
    public Integer getSystemId() {
        return systemId;
    }

    public void setSystemId(Integer systemId) {
        this.systemId = systemId;
    }

    @Basic
    @Column(name = "SYSTEM_NAME", nullable = false, length = 45)
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
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

        BbasSystemIdEntity that = (BbasSystemIdEntity) o;

        if (systemId != null ? !systemId.equals(that.systemId) : that.systemId != null) {
            return false;
        }
        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) {
            return false;
        }
        if (majorName != null ? !majorName.equals(that.majorName) : that.majorName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = systemId != null ? systemId.hashCode() : 0;
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        result = 31 * result + (majorName != null ? majorName.hashCode() : 0);
        return result;
    }
}
