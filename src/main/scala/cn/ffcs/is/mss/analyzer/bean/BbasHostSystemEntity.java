package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/27 15:07
 * @Modified By
 */
@Entity
@Table(name = "BBAS_HOST_SYSTEM", schema = "SDFS", catalog = "")
public class BbasHostSystemEntity {

    private String host;
    private String systemName;
    private String majorName;
    private String feature;

    @Id
    @Column(name = "HOST", nullable = false, length = 100)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
    @Column(name = "MAJOR_NAME", nullable = false, length = 100)
    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    @Basic
    @Column(name = "FEATURE", nullable = false, length = 45)
    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasHostSystemEntity that = (BbasHostSystemEntity) o;

        if (host != null ? !host.equals(that.host) : that.host != null) {
            return false;
        }
        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) {
            return false;
        }
        if (majorName != null ? !majorName.equals(that.majorName) : that.majorName != null) {
            return false;
        }
        if (feature != null ? !feature.equals(that.feature) : that.feature != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        result = 31 * result + (majorName != null ? majorName.hashCode() : 0);
        result = 31 * result + (feature != null ? feature.hashCode() : 0);
        return result;
    }
}
