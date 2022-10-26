package cn.ffcs.is.mss.analyzer.bean;/**
 * @title BbasDownloadCountWarnImproveEntityPK
 * @author PlatinaBoy
 * @date 2021-07-22 16:07
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @ClassName BbasDownloadCountWarnImproveEntityPK
 * @date 2021/7/22 16:07
 * @Author
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class BbasDownloadCountWarnImproveEntityPK implements Serializable {
    private Timestamp warnDatetime;
    private String username;
    private String downloadSystem;

    @Column(name = "WARN_DATETIME")
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "USERNAME")
    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "DOWNLOAD_SYSTEM")
    @Id
    public String getDownloadSystem() {
        return downloadSystem;
    }

    public void setDownloadSystem(String downloadSystem) {
        this.downloadSystem = downloadSystem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BbasDownloadCountWarnImproveEntityPK that = (BbasDownloadCountWarnImproveEntityPK) o;
        return Objects.equals(warnDatetime, that.warnDatetime) && Objects.equals(username, that.username) && Objects.equals(downloadSystem, that.downloadSystem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, username, downloadSystem);
    }
}
