package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/6/26 14:40
 * @Modified By
 */
@Entity
@Table(name = "BBAS_SINGLE_SYSTEM_OCTETS_REGRESSION", schema = "SDFS", catalog = "")
@IdClass(BbasSingleSystemOctetsRegressionEntityPK.class)
public class BbasSingleSystemOctetsRegressionEntity {

    private Date regressionDate;
    private String systemName;
    private String regressionValueText;

    @Id
    @Column(name = "REGRESSION_DATE", nullable = false)
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
    }

    @Id
    @Column(name = "SYSTEM_NAME", nullable = false, length = 40)
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Basic
    @Column(name = "REGRESSION_VALUE_TEXT", nullable = false, length = -1)
    public String getRegressionValueText() {
        return regressionValueText;
    }

    public void setRegressionValueText(String regressionValueText) {
        this.regressionValueText = regressionValueText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasSingleSystemOctetsRegressionEntity that = (BbasSingleSystemOctetsRegressionEntity) o;
        return Objects.equals(regressionDate, that.regressionDate) &&
            Objects.equals(systemName, that.systemName) &&
            Objects.equals(regressionValueText, that.regressionValueText);
    }

    @Override
    public int hashCode() {

        return Objects.hash(regressionDate, systemName, regressionValueText);
    }
}
