package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/1/10 17:40
 * @Modified By
 */
@Entity
@Table(name = "BBAS_USED_SYSTEM_DELTA_TEMP", schema = "SDFS", catalog = "")
@IdClass(BbasUsedSystemDeltaTempEntityPK.class)
public class BbasUsedSystemDeltaTempEntity {

    private String username;
    private Date date;
    private String deltaText;

    @Id
    @Column(name = "USERNAME", nullable = false, length = 45)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Id
    @Column(name = "DATE", nullable = false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Basic
    @Column(name = "DELTA_TEXT", nullable = false, length = 45)
    public String getDeltaText() {
        return deltaText;
    }

    public void setDeltaText(String deltaText) {
        this.deltaText = deltaText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasUsedSystemDeltaTempEntity that = (BbasUsedSystemDeltaTempEntity) o;

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
        if (date != null ? !date.equals(that.date) : that.date != null) {
            return false;
        }
        if (deltaText != null ? !deltaText.equals(that.deltaText) : that.deltaText != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (deltaText != null ? deltaText.hashCode() : 0);
        return result;
    }
}
