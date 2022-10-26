package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/23 下午7:51
 * @Modified By
 */
@Entity
@Table(name = "BBAS_CLUSTER_USED_PLACE", schema = "SDFS", catalog = "")
public class BbasClusterUsedPlaceEntity {

    private Integer clusterId;
    private String usedLoginPlace;

    @Id
    @Column(name = "CLUSTER_ID", nullable = false)
    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
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

        BbasClusterUsedPlaceEntity that = (BbasClusterUsedPlaceEntity) o;

        if (clusterId != null ? !clusterId.equals(that.clusterId) : that.clusterId != null) {
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
        int result = clusterId != null ? clusterId.hashCode() : 0;
        result = 31 * result + (usedLoginPlace != null ? usedLoginPlace.hashCode() : 0);
        return result;
    }
}
