package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/23 下午7:51
 * @Modified By
 */
@Entity
@Table(name = "BBAS_WARN_LEVEL", schema = "SDFS", catalog = "")
public class BbasWarnLevelEntity {

    private Integer warnLevel;
    private Double warnValue;
    private String warnName;

    @Id
    @Column(name = "WARN_LEVEL", nullable = false)
    public Integer getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(Integer warnLevel) {
        this.warnLevel = warnLevel;
    }

    @Basic
    @Column(name = "WARN_VALUE", nullable = false, precision = 0)
    public Double getWarnValue() {
        return warnValue;
    }

    public void setWarnValue(Double warnValue) {
        this.warnValue = warnValue;
    }

    @Basic
    @Column(name = "WARN_NAME", nullable = false, length = 45)
    public String getWarnName() {
        return warnName;
    }

    public void setWarnName(String warnName) {
        this.warnName = warnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasWarnLevelEntity that = (BbasWarnLevelEntity) o;

        if (warnLevel != null ? !warnLevel.equals(that.warnLevel) : that.warnLevel != null) {
            return false;
        }
        if (warnValue != null ? !warnValue.equals(that.warnValue) : that.warnValue != null) {
            return false;
        }
        if (warnName != null ? !warnName.equals(that.warnName) : that.warnName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnLevel != null ? warnLevel.hashCode() : 0;
        result = 31 * result + (warnValue != null ? warnValue.hashCode() : 0);
        result = 31 * result + (warnName != null ? warnName.hashCode() : 0);
        return result;
    }
}
