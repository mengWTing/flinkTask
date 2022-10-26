package cn.ffcs.is.mss.analyzer.bean;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/20 10:43
 * @Modified By
 */
@Entity
@Table(name = "BBAS_EXTREMELY_ACTIVE_SYSTEM", schema = "SDFS", catalog = "")
@IdClass(BbasExtremelyActiveSystemEntityPK.class)
public class BbasExtremelyActiveSystemEntity {

    private Timestamp activeDatetime;
    private String systemName;
    private String information;
    private String sourceIp;
    private String destIp;

    @Id
    @Column(name = "ACTIVE_DATETIME", nullable = false)
    public Timestamp getActiveDatetime() {
        return activeDatetime;
    }

    public void setActiveDatetime(Timestamp activeDatetime) {
        this.activeDatetime = activeDatetime;
    }

    @Id
    @Column(name = "SYSTEM_NAME", nullable = false, length = 45)
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Basic
    @Column(name = "INFORMATION", nullable = false, length = -1)
    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BbasExtremelyActiveSystemEntity that = (BbasExtremelyActiveSystemEntity) o;
        return Objects.equals(activeDatetime, that.activeDatetime) &&
            Objects.equals(systemName, that.systemName) &&
            Objects.equals(information, that.information);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeDatetime, systemName, information);
    }

    @Basic
    @Column(name = "SOURCE_IP")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @Basic
    @Column(name = "DEST_IP")
    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }
}
