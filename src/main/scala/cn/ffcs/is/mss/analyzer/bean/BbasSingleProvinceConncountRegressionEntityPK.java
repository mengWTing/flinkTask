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
public class BbasSingleProvinceConncountRegressionEntityPK implements Serializable {

    private Date regressionDate;
    private String provinceName;

    @Column(name = "REGRESSION_DATE", nullable = false)
    @Id
    public Date getRegressionDate() {
        return regressionDate;
    }

    public void setRegressionDate(Date regressionDate) {
        this.regressionDate = regressionDate;
    }

    @Column(name = "PROVINCE_NAME", nullable = false, length = 45)
    @Id
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasSingleProvinceConncountRegressionEntityPK that = (BbasSingleProvinceConncountRegressionEntityPK) o;
        return Objects.equals(regressionDate, that.regressionDate) &&
            Objects.equals(provinceName, that.provinceName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(regressionDate, provinceName);
    }
}
