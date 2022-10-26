package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "BBAS_SINGLE_PROVINCE_CONNCOUNT_REGRESSION1", schema = "SDFS", catalog = "")
@IdClass(BbasSingleProvinceConncountRegression1EntityPK.class)
public class BbasSingleProvinceConncountRegression1Entity {
    private Date regressionDate;
    private String provinceName;
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
    @Column(name = "PROVINCE_NAME")
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
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

        BbasSingleProvinceConncountRegression1Entity that = (BbasSingleProvinceConncountRegression1Entity) o;

        if (regressionDate != null ? !regressionDate.equals(that.regressionDate) : that.regressionDate != null)
            return false;
        if (provinceName != null ? !provinceName.equals(that.provinceName) : that.provinceName != null) return false;
        if (regressionValueText != null ? !regressionValueText.equals(that.regressionValueText) : that.regressionValueText != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = regressionDate != null ? regressionDate.hashCode() : 0;
        result = 31 * result + (provinceName != null ? provinceName.hashCode() : 0);
        result = 31 * result + (regressionValueText != null ? regressionValueText.hashCode() : 0);
        return result;
    }
}
