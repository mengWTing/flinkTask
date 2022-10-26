package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "BBAS_SINGLE_SYSTEM_CONNCOUNT_REGRESSION1", schema = "SDFS", catalog = "")
@IdClass(BbasSingleSystemConncountRegression1EntityPK.class)
public class BbasSingleSystemConncountRegression1Entity {
    private Date regressionDate;
    private String systemName;
    private String regressionValueText;

    @Id
    @Column(name = "REGRESSION_DATE")
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
    }

    @Id
    @Column(name = "SYSTEM_NAME")
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Basic
    @Column(name = "REGRESSION_VALUE_TEXT")
    public String getRegressionValueText() {
        return regressionValueText;
    }

    public void setRegressionValueText(String regressionValueText) {
        this.regressionValueText = regressionValueText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BbasSingleSystemConncountRegression1Entity that = (BbasSingleSystemConncountRegression1Entity) o;

        if (regressionDate != null ? !regressionDate.equals(that.regressionDate) : that.regressionDate != null)
            return false;
        if (systemName != null ? !systemName.equals(that.systemName) : that.systemName != null) return false;
        if (regressionValueText != null ? !regressionValueText.equals(that.regressionValueText) : that.regressionValueText != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = regressionDate != null ? regressionDate.hashCode() : 0;
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        result = 31 * result + (regressionValueText != null ? regressionValueText.hashCode() : 0);
        return result;
    }
}
