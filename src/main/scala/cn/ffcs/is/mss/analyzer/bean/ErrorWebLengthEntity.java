package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.*;

@Entity
@Table(name = "ERROR_WEB_LENGTH", schema = "SDFS", catalog = "")
public class ErrorWebLengthEntity {
    private String domainName;
    private Integer errorWebLength;

    @Id
    @Column(name = "DOMAIN_NAME")
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Basic
    @Column(name = "ERROR_WEB_LENGTH")
    public Integer getErrorWebLength() {
        return errorWebLength;
    }

    public void setErrorWebLength(Integer errorWebLength) {
        this.errorWebLength = errorWebLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorWebLengthEntity that = (ErrorWebLengthEntity) o;

        if (domainName != null ? !domainName.equals(that.domainName) : that.domainName != null) return false;
        if (errorWebLength != null ? !errorWebLength.equals(that.errorWebLength) : that.errorWebLength != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainName != null ? domainName.hashCode() : 0;
        result = 31 * result + (errorWebLength != null ? errorWebLength.hashCode() : 0);
        return result;
    }
}
