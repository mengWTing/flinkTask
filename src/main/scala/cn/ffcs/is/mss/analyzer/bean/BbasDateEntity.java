package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/22 11:00
 * @Modified By
 */
@Entity
@Table(name = "BBAS_DATE", schema = "SDFS", catalog = "")
public class BbasDateEntity {

    private Date date;
    private Integer flag;

    @Id
    @Column(name = "DATE", nullable = false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Basic
    @Column(name = "FLAG", nullable = false)
    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasDateEntity that = (BbasDateEntity) o;

        if (date != null ? !date.equals(that.date) : that.date != null) {
            return false;
        }
        if (flag != null ? !flag.equals(that.flag) : that.flag != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (flag != null ? flag.hashCode() : 0);
        return result;
    }
}
