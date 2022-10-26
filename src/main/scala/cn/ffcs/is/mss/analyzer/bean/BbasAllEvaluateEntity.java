package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/20 10:43
 * @Modified By
 */
@Entity
@Table(name = "BBAS_ALL_EVALUATE", schema = "SDFS", catalog = "")
public class BbasAllEvaluateEntity {

    private Timestamp evaluateDatetime;
    private Double evaluateSocre;

    @Id
    @Column(name = "EVALUATE_DATETIME", nullable = false)
    public Timestamp getEvaluateDatetime() {
        return evaluateDatetime;
    }

    public void setEvaluateDatetime(Timestamp evaluateDatetime) {
        this.evaluateDatetime = evaluateDatetime;
    }

    @Basic
    @Column(name = "EVALUATE_SOCRE", nullable = false, precision = 0)
    public Double getEvaluateSocre() {
        return evaluateSocre;
    }

    public void setEvaluateSocre(Double evaluateSocre) {
        this.evaluateSocre = evaluateSocre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasAllEvaluateEntity that = (BbasAllEvaluateEntity) o;
        return Objects.equals(evaluateDatetime, that.evaluateDatetime) &&
            Objects.equals(evaluateSocre, that.evaluateSocre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluateDatetime, evaluateSocre);
    }
}
