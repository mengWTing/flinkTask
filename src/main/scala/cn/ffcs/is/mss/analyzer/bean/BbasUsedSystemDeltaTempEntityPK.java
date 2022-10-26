package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/1/10 17:40
 * @Modified By
 */
public class BbasUsedSystemDeltaTempEntityPK implements Serializable {

    private String username;
    private Date date;

    @Column(name = "USERNAME", nullable = false, length = 45)
    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "DATE", nullable = false)
    @Id
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasUsedSystemDeltaTempEntityPK that = (BbasUsedSystemDeltaTempEntityPK) o;

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
        if (date != null ? !date.equals(that.date) : that.date != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
