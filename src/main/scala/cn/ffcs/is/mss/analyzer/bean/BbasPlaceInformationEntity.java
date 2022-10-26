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
@Table(name = "BBAS_PLACE_INFORMATION", schema = "SDFS", catalog = "")
public class BbasPlaceInformationEntity {

    private Integer placeId;
    private String placeName;
    private Double longitude;
    private Double latitude;
    private Integer isForeign;

    @Basic
    @Column(name = "PLACE_ID", nullable = false)
    public Integer getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Integer placeId) {
        this.placeId = placeId;
    }

    @Id
    @Column(name = "PLACE_NAME", nullable = false, length = 45)
    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    @Basic
    @Column(name = "LONGITUDE", nullable = false, precision = 0)
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Basic
    @Column(name = "LATITUDE", nullable = false, precision = 0)
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Basic
    @Column(name = "IS_FOREIGN", nullable = false)
    public Integer getIsForeign() {
        return isForeign;
    }

    public void setIsForeign(Integer isForeign) {
        this.isForeign = isForeign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BbasPlaceInformationEntity that = (BbasPlaceInformationEntity) o;

        if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) {
            return false;
        }
        if (placeName != null ? !placeName.equals(that.placeName) : that.placeName != null) {
            return false;
        }
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) {
            return false;
        }
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) {
            return false;
        }
        if (isForeign != null ? !isForeign.equals(that.isForeign) : that.isForeign != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = placeId != null ? placeId.hashCode() : 0;
        result = 31 * result + (placeName != null ? placeName.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (isForeign != null ? isForeign.hashCode() : 0);
        return result;
    }
}
