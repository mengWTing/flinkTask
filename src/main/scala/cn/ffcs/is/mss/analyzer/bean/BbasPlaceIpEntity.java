package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/23 下午7:51
 * @Modified By
 */
@Entity
@Table(name = "BBAS_PLACE_IP", schema = "SDFS", catalog = "")
public class BbasPlaceIpEntity {

    private String placeName;
    private String ip;

    @Basic
    @Column(name = "PLACE_NAME", nullable = false, length = 45)
    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    @Basic
    @Column(name = "IP", nullable = false, length = 45)
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasPlaceIpEntity that = (BbasPlaceIpEntity) o;

        if (placeName != null ? !placeName.equals(that.placeName) : that.placeName != null) {
            return false;
        }
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = placeName != null ? placeName.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        return result;
    }
}
