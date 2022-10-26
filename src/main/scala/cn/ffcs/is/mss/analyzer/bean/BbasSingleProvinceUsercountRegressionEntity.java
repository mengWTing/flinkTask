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
@Table(name = "BBAS_SINGLE_PROVINCE_USERCOUNT_REGRESSION", schema = "SDFS", catalog = "")
@IdClass(BbasSingleProvinceUsercountRegressionEntityPK.class)
public class BbasSingleProvinceUsercountRegressionEntity {

    private Date regressionDate;
    private String provinceName;
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
    @Column(name = "PROVINCE_NAME", nullable = false, length = 45)
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
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
        BbasSingleProvinceUsercountRegressionEntity that = (BbasSingleProvinceUsercountRegressionEntity) o;
        return Objects.equals(regressionDate, that.regressionDate) &&
            Objects.equals(provinceName, that.provinceName) &&
            Objects.equals(regressionValueText, that.regressionValueText);
    }

    @Override
    public int hashCode() {

        return Objects.hash(regressionDate, provinceName, regressionValueText);
    }
}
