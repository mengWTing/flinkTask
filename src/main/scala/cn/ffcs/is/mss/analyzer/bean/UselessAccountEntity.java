package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author liangzhaosuo
 * @title UselessAccountEntity
 * @date 2021-01-04 14:34
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */


@Entity
@Table(name = "USELESS_ACCOUNT", schema = "SDFS", catalog = "")
public class UselessAccountEntity {
    private Long id;
    private String userName;
    private Timestamp latestLoginTime;
    private Timestamp createTime;

    @Id
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "USER_NAME", nullable = true, length = 255)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "LATEST_LOGIN_TIME", nullable = true)
    public Timestamp getLatestLoginTime() {
        return latestLoginTime;
    }

    public void setLatestLoginTime(Timestamp latestLoginTime) {
        this.latestLoginTime = latestLoginTime;
    }

    @Basic
    @Column(name = "CREATE_TIME", nullable = true)
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UselessAccountEntity that = (UselessAccountEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (latestLoginTime != null ? !latestLoginTime.equals(that.latestLoginTime) : that.latestLoginTime != null)
            return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (latestLoginTime != null ? latestLoginTime.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        return result;
    }
}
