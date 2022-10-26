package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Date;

public class BbasSingleSystemConncountRegression1EntityPK implements Serializable {
    private Date regressionDate;
    private String systemName;

    @Column(name = "REGRESSION_DATE")
    @Id
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
    }

    @Column(name = "SYSTEM_NAME")
    @Id
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BbasSingleSystemConncountRegression1EntityPK that = (BbasSingleSystemConncountRegression1EntityPK) o;

        if (regressionDate != null ? !regressionDate.equals(that.regressionDate) : that.regressionDate != null)
            return false;
        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = regressionDate != null ? regressionDate.hashCode() : 0;
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        return result;
    }
}
