package cn.ffcs.is.mss.analyzer.bean;/**
 * @title OaSystemCrawlerWarningEntityPK
 * @author PlatinaBoy
 * @date 2021-07-15 16:57
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @ClassName OaSystemCrawlerWarningEntityPK
 * @date 2021/7/15 16:57
 * @Author
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class OaSystemCrawlerWarningEntityPK implements Serializable {
    private Timestamp startTime;
    private String userName;

    @Column(name = "START_TIME")
    @Id
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Column(name = "USER_NAME")
    @Id
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OaSystemCrawlerWarningEntityPK that = (OaSystemCrawlerWarningEntityPK) o;
        return Objects.equals(startTime, that.startTime) && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, userName);
    }
}
