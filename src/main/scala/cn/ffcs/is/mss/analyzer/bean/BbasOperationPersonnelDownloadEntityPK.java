package cn.ffcs.is.mss.analyzer.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/11/21 19:04
 * @Modified By
 */
public class BbasOperationPersonnelDownloadEntityPK implements Serializable {

    private Timestamp warnDatetime;
    private String username;
    private String downloadFimeName;

    @Column(name = "WARN_DATETIME", nullable = false)
    @Id
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Column(name = "USERNAME", nullable = false, length = 100)
    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "DOWNLOAD_FIME_NAME", nullable = false, length = 45)
    @Id
    public String getDownloadFimeName() {
        return downloadFimeName;
    }

    public void setDownloadFimeName(String downloadFimeName) {
        this.downloadFimeName = downloadFimeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasOperationPersonnelDownloadEntityPK that = (BbasOperationPersonnelDownloadEntityPK) o;
        return Objects.equals(warnDatetime, that.warnDatetime) &&
            Objects.equals(username, that.username) &&
            Objects.equals(downloadFimeName, that.downloadFimeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warnDatetime, username, downloadFimeName);
    }
}
