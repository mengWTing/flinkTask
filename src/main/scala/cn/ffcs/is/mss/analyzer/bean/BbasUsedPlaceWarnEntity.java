package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/6 下午4:53
 * @Modified By
 */
@Entity
@Table(name = "BBAS_USED_PLACE_WARN", schema = "SDFS", catalog = "")
public class BbasUsedPlaceWarnEntity {

    private Timestamp warnDatetime;
    private String userName;
    private String sourceIp;
    private String loginPlace;
    private String destinationIp;
    private String loginSystem;
    private String operation;
    private Integer clusterId;
    private String clusterDescribe;
    private String usedLoginPlace;

    @Basic
    @Column(name = "WARN_DATETIME", nullable = false)
    public Timestamp getWarnDatetime() {
        return warnDatetime;
    }

    public void setWarnDatetime(Timestamp warnDatetime) {
        this.warnDatetime = warnDatetime;
    }

    @Basic
    @Column(name = "USER_NAME", nullable = false, length = 45)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "SOURCE_IP", nullable = false, length = 45)
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "LOGIN_PLACE", nullable = false, length = 45)
    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    @Basic
    @Column(name = "DESTINATION_IP", nullable = false, length = 45)
    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    @Basic
    @Column(name = "LOGIN_SYSTEM", nullable = false, length = 45)
    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Basic
    @Column(name = "OPERATION", nullable = false, length = 45)
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Basic
    @Column(name = "CLUSTER_ID", nullable = false)
    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    @Basic
    @Column(name = "CLUSTER_DESCRIBE", nullable = false, length = 200)
    public String getClusterDescribe() {
        return clusterDescribe;
    }

    public void setClusterDescribe(String clusterDescribe) {
        this.clusterDescribe = clusterDescribe;
    }

    @Basic
    @Column(name = "USED_LOGIN_PLACE", nullable = false, length = 1000)
    public String getUsedLoginPlace() {
        return usedLoginPlace;
    }

    public void setUsedLoginPlace(String usedLoginPlace) {
        this.usedLoginPlace = usedLoginPlace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasUsedPlaceWarnEntity that = (BbasUsedPlaceWarnEntity) o;

        if (warnDatetime != null ? !warnDatetime.equals(that.warnDatetime)
            : that.warnDatetime != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) {
            return false;
        }
        if (loginPlace != null ? !loginPlace.equals(that.loginPlace) : that.loginPlace != null) {
            return false;
        }
        if (destinationIp != null ? !destinationIp.equals(that.destinationIp)
            : that.destinationIp != null) {
            return false;
        }
        if (loginSystem != null ? !loginSystem.equals(that.loginSystem)
            : that.loginSystem != null) {
            return false;
        }
        if (operation != null ? !operation.equals(that.operation) : that.operation != null) {
            return false;
        }
        if (clusterId != null ? !clusterId.equals(that.clusterId) : that.clusterId != null) {
            return false;
        }
        if (clusterDescribe != null ? !clusterDescribe.equals(that.clusterDescribe)
            : that.clusterDescribe != null) {
            return false;
        }
        if (usedLoginPlace != null ? !usedLoginPlace.equals(that.usedLoginPlace)
            : that.usedLoginPlace != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = warnDatetime != null ? warnDatetime.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (loginPlace != null ? loginPlace.hashCode() : 0);
        result = 31 * result + (destinationIp != null ? destinationIp.hashCode() : 0);
        result = 31 * result + (loginSystem != null ? loginSystem.hashCode() : 0);
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
        result = 31 * result + (clusterDescribe != null ? clusterDescribe.hashCode() : 0);
        result = 31 * result + (usedLoginPlace != null ? usedLoginPlace.hashCode() : 0);
        return result;
    }
}
