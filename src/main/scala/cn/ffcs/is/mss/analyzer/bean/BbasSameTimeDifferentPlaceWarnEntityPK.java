/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2020-03-30 18:17:36
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author chenwei
 * @date 2020-03-30 18:17:36
 * @title BbasSameTimeDifferentPlaceWarnEntityPK
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
public class BbasSameTimeDifferentPlaceWarnEntityPK implements Serializable {

    private Timestamp warnDatetime;
    private String userName;
    private String sourceIp;
    private String destinationIp;

    @Column(name = "WARN_DATETIME", nullable = false)
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "USER_NAME", nullable = false, length = 45)
    @Id
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "SOURCE_IP", nullable = false, length = 45)
    @Id
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Column(name = "DESTINATION_IP", nullable = false, length = 45)
    @Id
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasSameTimeDifferentPlaceWarnEntityPK that = (BbasSameTimeDifferentPlaceWarnEntityPK) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(userName, that.userName) &&
            Objects.equals(sourceIp, that.sourceIp) &&
            Objects.equals(destinationIp, that.destinationIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, userName, sourceIp, destinationIp);
    }
}
