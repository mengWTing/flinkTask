package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

public class CrawlerWarningEntityPK implements Serializable {
    private String userName;
    private Timestamp startDatetime;

    @Column(name = "USER_NAME")
    @Id
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "START_DATETIME")
    @Id
    public Timestamp getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Timestamp startDatetime) {
        this.startDatetime = startDatetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrawlerWarningEntityPK that = (CrawlerWarningEntityPK) o;

        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (startDatetime != null ? !startDatetime.equals(that.startDatetime) : that.startDatetime != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (startDatetime != null ? startDatetime.hashCode() : 0);
        return result;
    }
}
