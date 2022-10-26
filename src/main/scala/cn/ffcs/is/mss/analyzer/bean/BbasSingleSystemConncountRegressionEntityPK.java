package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/6/26 14:40
 * @Modified By
 */
public class BbasSingleSystemConncountRegressionEntityPK implements Serializable {

    private Date regressionDate;
    private String systemName;

    @Column(name = "REGRESSION_DATE", nullable = false)
    @Id
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
    }

    @Column(name = "SYSTEM_NAME", nullable = false, length = 40)
    @Id
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasSingleSystemConncountRegressionEntityPK that = (BbasSingleSystemConncountRegressionEntityPK) o;
        return Objects.equals(regressionDate, that.regressionDate) &&
            Objects.equals(systemName, that.systemName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(regressionDate, systemName);
    }
}
