/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-01-22 16:43:25
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
 * @date 2019-01-22 16:43:25
 * @title BbasDownloadCountWarnEntityPK
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
public class BbasDownloadCountWarnEntityPK implements Serializable {

    private Timestamp warnDatetime;
    private String username;
    private String downloadSystem;

    @Column(name = "WARN_DATETIME", nullable = false)
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "USERNAME", nullable = false, length = 45)
    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "DOWNLOAD_SYSTEM", nullable = false, length = 45)
    @Id
    public String getDownloadSystem() {
        return downloadSystem;
    }

    public void setDownloadSystem(String downloadSystem) {
        this.downloadSystem = downloadSystem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasDownloadCountWarnEntityPK that = (BbasDownloadCountWarnEntityPK) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(username, that.username) &&
            Objects.equals(downloadSystem, that.downloadSystem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, username, downloadSystem);
    }
}
