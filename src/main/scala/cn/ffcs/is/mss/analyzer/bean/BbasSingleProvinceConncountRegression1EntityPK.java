package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Date;

public class BbasSingleProvinceConncountRegression1EntityPK implements Serializable {
    private Date regressionDate;
    private String provinceName;

    @Column(name = "REGRESSION_DATE")
    @Id
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
    }

    @Column(name = "PROVINCE_NAME")
    @Id
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BbasSingleProvinceConncountRegression1EntityPK that = (BbasSingleProvinceConncountRegression1EntityPK) o;

        if (regressionDate != null ? !regressionDate.equals(that.regressionDate) : that.regressionDate != null)
            return false;
        if (provinceName != null ? !provinceName.equals(that.provinceName) : that.provinceName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = regressionDate != null ? regressionDate.hashCode() : 0;
        result = 31 * result + (provinceName != null ? provinceName.hashCode() : 0);
        return result;
    }
}
