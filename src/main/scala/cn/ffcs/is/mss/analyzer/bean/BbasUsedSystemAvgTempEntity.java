package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/1/10 17:40
 * @Modified By
 */
@Entity
@Table(name = "BBAS_USED_SYSTEM_AVG_TEMP", schema = "SDFS", catalog = "")
public class BbasUsedSystemAvgTempEntity {

    private String username;
    private String avgText;

    @Id
    @Column(name = "USERNAME", nullable = false, length = 45)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "AVG_TEXT", nullable = false, length = -1)
    public String getAvgText() {
        return avgText;
    }

    public void setAvgText(String avgText) {
        this.avgText = avgText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasUsedSystemAvgTempEntity that = (BbasUsedSystemAvgTempEntity) o;

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
        if (avgText != null ? !avgText.equals(that.avgText) : that.avgText != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (avgText != null ? avgText.hashCode() : 0);
        return result;
    }
}
