package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @ClassName UselessAccountImproveEntity
 * @date 2021/12/9 11:52
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "useless_account_improve", schema = "bigdata", catalog = "")
public class UselessAccountImproveEntity {
    private long id;
    private String userName;
    private Timestamp createTime;
    private Timestamp latestLoginTime;
    private String lastSourceIp;
    private String lastLoginSystem;
    private String lastLoginMajor;
    private String lastLoginPlace;
    private String lastDescIp;
    private String lastUsedPlace;
    private String lastIsRemote;
    private String lastIsDownload;
    private String lastIsDownloadSuccess;
    private String lastDownloadFile;

    @Id
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "CREATE_TIME")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "LATEST_LOGIN_TIME")
    public Timestamp getLatestLoginTime() {
        return latestLoginTime;
    }

    public void setLatestLoginTime(Timestamp latestLoginTime) {
        this.latestLoginTime = latestLoginTime;
    }

    @Basic
    @Column(name = "LAST_SOURCE_IP")
    public String getLastSourceIp() {
        return lastSourceIp;
    }

    public void setLastSourceIp(String lastSourceIp) {
        this.lastSourceIp = lastSourceIp;
    }

    @Basic
    @Column(name = "LAST_LOGIN_SYSTEM")
    public String getLastLoginSystem() {
        return lastLoginSystem;
    }

    public void setLastLoginSystem(String lastLoginSystem) {
        this.lastLoginSystem = lastLoginSystem;
    }

    @Basic
    @Column(name = "LAST_LOGIN_MAJOR")
    public String getLastLoginMajor() {
        return lastLoginMajor;
    }

    public void setLastLoginMajor(String lastLoginMajor) {
        this.lastLoginMajor = lastLoginMajor;
    }

    @Basic
    @Column(name = "LAST_LOGIN_PLACE")
    public String getLastLoginPlace() {
        return lastLoginPlace;
    }

    public void setLastLoginPlace(String lastLoginPlace) {
        this.lastLoginPlace = lastLoginPlace;
    }

    @Basic
    @Column(name = "LAST_DESC_IP")
    public String getLastDescIp() {
        return lastDescIp;
    }

    public void setLastDescIp(String lastDescIp) {
        this.lastDescIp = lastDescIp;
    }

    @Basic
    @Column(name = "LAST_USED_PLACE")
    public String getLastUsedPlace() {
        return lastUsedPlace;
    }

    public void setLastUsedPlace(String lastUsedPlace) {
        this.lastUsedPlace = lastUsedPlace;
    }

    @Basic
    @Column(name = "LAST_IS_REMOTE")
    public String getLastIsRemote() {
        return lastIsRemote;
    }

    public void setLastIsRemote(String lastIsRemote) {
        this.lastIsRemote = lastIsRemote;
    }

    @Basic
    @Column(name = "LAST_IS_DOWNLOAD")
    public String getLastIsDownload() {
        return lastIsDownload;
    }

    public void setLastIsDownload(String lastIsDownload) {
        this.lastIsDownload = lastIsDownload;
    }

    @Basic
    @Column(name = "LAST_IS_DOWNLOAD_SUCCESS")
    public String getLastIsDownloadSuccess() {
        return lastIsDownloadSuccess;
    }

    public void setLastIsDownloadSuccess(String lastIsDownloadSuccess) {
        this.lastIsDownloadSuccess = lastIsDownloadSuccess;
    }

    @Basic
    @Column(name = "LAST_DOWNLOAD_FILE")
    public String getLastDownloadFile() {
        return lastDownloadFile;
    }

    public void setLastDownloadFile(String lastDownloadFile) {
        this.lastDownloadFile = lastDownloadFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UselessAccountImproveEntity that = (UselessAccountImproveEntity) o;
        return id == that.id && Objects.equals(userName, that.userName) && Objects.equals(createTime, that.createTime) && Objects.equals(latestLoginTime, that.latestLoginTime) && Objects.equals(lastSourceIp, that.lastSourceIp) && Objects.equals(lastLoginSystem, that.lastLoginSystem) && Objects.equals(lastLoginMajor, that.lastLoginMajor) && Objects.equals(lastLoginPlace, that.lastLoginPlace) && Objects.equals(lastDescIp, that.lastDescIp) && Objects.equals(lastUsedPlace, that.lastUsedPlace) && Objects.equals(lastIsRemote, that.lastIsRemote) && Objects.equals(lastIsDownload, that.lastIsDownload) && Objects.equals(lastIsDownloadSuccess, that.lastIsDownloadSuccess) && Objects.equals(lastDownloadFile, that.lastDownloadFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, createTime, latestLoginTime, lastSourceIp, lastLoginSystem, lastLoginMajor, lastLoginPlace, lastDescIp, lastUsedPlace, lastIsRemote, lastIsDownload, lastIsDownloadSuccess, lastDownloadFile);
    }
}
