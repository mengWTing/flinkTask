/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-04-16 15:00:22
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.bean;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author chenwei
 * @date 2019-04-16 15:00:22
 * @title CtPostInfoEntity
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
@Entity
@Table(name = "ct_post_info", schema = "SDFS", catalog = "")
public class CtPostInfoEntity {

    private Long id;
    private String loginName;
    private String userName;
    private String ctUserEname;
    private String ctTelephoneNumber;
    private String ctMail;
    private String parentCode;
    private String ctCorpCode;
    private String ctGender;
    private String ctIdentityNumber;
    private String ctPreferredMobile;
    private Integer userStatus;
    private BigDecimal showNum;
    private String ctHrUserCode;
    private String ctPositionType;
    private String ctTitle;
    private String ctPositionName;
    private String ctPosLevelType;
    private String ctPositionLevel;
    private String ctPosLayerType;
    private String ctPositionLayer;
    private String ctPositionSequence;
    private String ctStatus;
    private String pType;
    private String hrPositionType;
    private String hrId;
    private String ctUserType;
    private String reserveAccount;
    private Long ctUserId;

    @Id
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "LOGIN_NAME", nullable = true, length = 40)
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @Basic
    @Column(name = "USER_NAME", nullable = true, length = 300)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "CT_USER_ENAME", nullable = true, length = 300)
    public String getCtUserEname() {
        return ctUserEname;
    }

    public void setCtUserEname(String ctUserEname) {
        this.ctUserEname = ctUserEname;
    }

    @Basic
    @Column(name = "CT_TELEPHONE_NUMBER", nullable = true, length = 30)
    public String getCtTelephoneNumber() {
        return ctTelephoneNumber;
    }

    public void setCtTelephoneNumber(String ctTelephoneNumber) {
        this.ctTelephoneNumber = ctTelephoneNumber;
    }

    @Basic
    @Column(name = "CT_MAIL", nullable = true, length = 50)
    public String getCtMail() {
        return ctMail;
    }

    public void setCtMail(String ctMail) {
        this.ctMail = ctMail;
    }

    @Basic
    @Column(name = "PARENT_CODE", nullable = true, length = 40)
    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    @Basic
    @Column(name = "CT_CORP_CODE", nullable = true, length = 40)
    public String getCtCorpCode() {
        return ctCorpCode;
    }

    public void setCtCorpCode(String ctCorpCode) {
        this.ctCorpCode = ctCorpCode;
    }

    @Basic
    @Column(name = "CT_GENDER", nullable = true, length = 20)
    public String getCtGender() {
        return ctGender;
    }

    public void setCtGender(String ctGender) {
        this.ctGender = ctGender;
    }

    @Basic
    @Column(name = "CT_IDENTITY_NUMBER", nullable = true, length = 28)
    public String getCtIdentityNumber() {
        return ctIdentityNumber;
    }

    public void setCtIdentityNumber(String ctIdentityNumber) {
        this.ctIdentityNumber = ctIdentityNumber;
    }

    @Basic
    @Column(name = "CT_PREFERRED_MOBILE", nullable = true, length = 30)
    public String getCtPreferredMobile() {
        return ctPreferredMobile;
    }

    public void setCtPreferredMobile(String ctPreferredMobile) {
        this.ctPreferredMobile = ctPreferredMobile;
    }

    @Basic
    @Column(name = "USER_STATUS", nullable = true)
    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    @Basic
    @Column(name = "SHOW_NUM", nullable = true, precision = 0)
    public BigDecimal getShowNum() {
        return showNum;
    }

    public void setShowNum(BigDecimal showNum) {
        this.showNum = showNum;
    }

    @Basic
    @Column(name = "CT_HR_USER_CODE", nullable = true, length = 40)
    public String getCtHrUserCode() {
        return ctHrUserCode;
    }

    public void setCtHrUserCode(String ctHrUserCode) {
        this.ctHrUserCode = ctHrUserCode;
    }

    @Basic
    @Column(name = "CT_POSITION_TYPE", nullable = true, length = 300)
    public String getCtPositionType() {
        return ctPositionType;
    }

    public void setCtPositionType(String ctPositionType) {
        this.ctPositionType = ctPositionType;
    }

    @Basic
    @Column(name = "CT_TITLE", nullable = true, length = 250)
    public String getCtTitle() {
        return ctTitle;
    }

    public void setCtTitle(String ctTitle) {
        this.ctTitle = ctTitle;
    }

    @Basic
    @Column(name = "CT_POSITION_NAME", nullable = true, length = 300)
    public String getCtPositionName() {
        return ctPositionName;
    }

    public void setCtPositionName(String ctPositionName) {
        this.ctPositionName = ctPositionName;
    }

    @Basic
    @Column(name = "CT_POS_LEVEL_TYPE", nullable = true, length = 40)
    public String getCtPosLevelType() {
        return ctPosLevelType;
    }

    public void setCtPosLevelType(String ctPosLevelType) {
        this.ctPosLevelType = ctPosLevelType;
    }

    @Basic
    @Column(name = "CT_POSITION_LEVEL", nullable = true, length = 40)
    public String getCtPositionLevel() {
        return ctPositionLevel;
    }

    public void setCtPositionLevel(String ctPositionLevel) {
        this.ctPositionLevel = ctPositionLevel;
    }

    @Basic
    @Column(name = "CT_POS_LAYER_TYPE", nullable = true, length = 40)
    public String getCtPosLayerType() {
        return ctPosLayerType;
    }

    public void setCtPosLayerType(String ctPosLayerType) {
        this.ctPosLayerType = ctPosLayerType;
    }

    @Basic
    @Column(name = "CT_POSITION_LAYER", nullable = true, length = 40)
    public String getCtPositionLayer() {
        return ctPositionLayer;
    }

    public void setCtPositionLayer(String ctPositionLayer) {
        this.ctPositionLayer = ctPositionLayer;
    }

    @Basic
    @Column(name = "CT_POSITION_SEQUENCE", nullable = true, length = 40)
    public String getCtPositionSequence() {
        return ctPositionSequence;
    }

    public void setCtPositionSequence(String ctPositionSequence) {
        this.ctPositionSequence = ctPositionSequence;
    }

    @Basic
    @Column(name = "CT_STATUS", nullable = true, length = 40)
    public String getCtStatus() {
        return ctStatus;
    }

    public void setCtStatus(String ctStatus) {
        this.ctStatus = ctStatus;
    }

    @Basic
    @Column(name = "P_TYPE", nullable = true, length = 40)
    public String getPType() {
        return pType;
    }

    public void setPType(String pType) {
        this.pType = pType;
    }

    @Basic
    @Column(name = "HR_POSITION_TYPE", nullable = true, length = 40)
    public String getHrPositionType() {
        return hrPositionType;
    }

    public void setHrPositionType(String hrPositionType) {
        this.hrPositionType = hrPositionType;
    }

    @Basic
    @Column(name = "HR_ID", nullable = true, length = 200)
    public String getHrId() {
        return hrId;
    }

    public void setHrId(String hrId) {
        this.hrId = hrId;
    }

    @Basic
    @Column(name = "CT_USER_TYPE", nullable = true, length = 40)
    public String getCtUserType() {
        return ctUserType;
    }

    public void setCtUserType(String ctUserType) {
        this.ctUserType = ctUserType;
    }

    @Basic
    @Column(name = "RESERVE_ACCOUNT", nullable = true, length = 128)
    public String getReserveAccount() {
        return reserveAccount;
    }

    public void setReserveAccount(String reserveAccount) {
        this.reserveAccount = reserveAccount;
    }

    @Basic
    @Column(name = "CT_USER_ID", nullable = true)
    public Long getCtUserId() {
        return ctUserId;
    }

    public void setCtUserId(Long ctUserId) {
        this.ctUserId = ctUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CtPostInfoEntity that = (CtPostInfoEntity) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(loginName, that.loginName) &&
            Objects.equals(userName, that.userName) &&
            Objects.equals(ctUserEname, that.ctUserEname) &&
            Objects.equals(ctTelephoneNumber, that.ctTelephoneNumber) &&
            Objects.equals(ctMail, that.ctMail) &&
            Objects.equals(parentCode, that.parentCode) &&
            Objects.equals(ctCorpCode, that.ctCorpCode) &&
            Objects.equals(ctGender, that.ctGender) &&
            Objects.equals(ctIdentityNumber, that.ctIdentityNumber) &&
            Objects.equals(ctPreferredMobile, that.ctPreferredMobile) &&
            Objects.equals(userStatus, that.userStatus) &&
            Objects.equals(showNum, that.showNum) &&
            Objects.equals(ctHrUserCode, that.ctHrUserCode) &&
            Objects.equals(ctPositionType, that.ctPositionType) &&
            Objects.equals(ctTitle, that.ctTitle) &&
            Objects.equals(ctPositionName, that.ctPositionName) &&
            Objects.equals(ctPosLevelType, that.ctPosLevelType) &&
            Objects.equals(ctPositionLevel, that.ctPositionLevel) &&
            Objects.equals(ctPosLayerType, that.ctPosLayerType) &&
            Objects.equals(ctPositionLayer, that.ctPositionLayer) &&
            Objects.equals(ctPositionSequence, that.ctPositionSequence) &&
            Objects.equals(ctStatus, that.ctStatus) &&
            Objects.equals(pType, that.pType) &&
            Objects.equals(hrPositionType, that.hrPositionType) &&
            Objects.equals(hrId, that.hrId) &&
            Objects.equals(ctUserType, that.ctUserType) &&
            Objects.equals(reserveAccount, that.reserveAccount) &&
            Objects.equals(ctUserId, that.ctUserId);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, loginName, userName, ctUserEname, ctTelephoneNumber, ctMail, parentCode,
                ctCorpCode, ctGender, ctIdentityNumber, ctPreferredMobile, userStatus, showNum,
                ctHrUserCode, ctPositionType, ctTitle, ctPositionName, ctPosLevelType,
                ctPositionLevel,
                ctPosLayerType, ctPositionLayer, ctPositionSequence, ctStatus, pType,
                hrPositionType, hrId,
                ctUserType, reserveAccount, ctUserId);
    }
}
