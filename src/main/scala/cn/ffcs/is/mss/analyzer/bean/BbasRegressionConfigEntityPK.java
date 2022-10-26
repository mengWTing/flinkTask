package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/1 下午5:29
 * @Modified By
 */
public class BbasRegressionConfigEntityPK implements Serializable {

    private String name;
    private String regressionType;

    @Column(name = "NAME", nullable = false, length = 45)
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "REGRESSION_TYPE", nullable = false, length = 45)
    @Id
    public String getRegressionType() {
        return regressionType;
    }

    public void setRegressionType(String regressionType) {
        this.regressionType = regressionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasRegressionConfigEntityPK that = (BbasRegressionConfigEntityPK) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (regressionType != null ? !regressionType.equals(that.regressionType)
            : that.regressionType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (regressionType != null ? regressionType.hashCode() : 0);
        return result;
    }
}
