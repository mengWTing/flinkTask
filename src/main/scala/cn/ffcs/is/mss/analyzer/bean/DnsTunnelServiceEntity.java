package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author PlatinaBoy
 * @title DnsTunnelServiceEntity
 * @date 2020-10-15 09:55
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
@Entity
@Table(name = "dns_tunnel_service", schema = "SDFS", catalog = "")
public class DnsTunnelServiceEntity {
    private Timestamp alertTime;
    private String queryDomainName;
    private String queryResult;
    private String replayCode;
    private String answerRrs;

    @Basic
    @Column(name = "ALERT_TIME")
    public Timestamp getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Timestamp alertTime) {
        this.alertTime = alertTime;
    }

    @Basic
    @Column(name = "QUERY_DOMAIN_NAME")
    public String getQueryDomainName() {
        return queryDomainName;
    }

    public void setQueryDomainName(String queryDomainName) {
        this.queryDomainName = queryDomainName;
    }

    @Basic
    @Column(name = "QUERY_RESULT")
    public String getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(String queryResult) {
        this.queryResult = queryResult;
    }

    @Basic
    @Column(name = "REPLAY_CODE")
    public String getReplayCode() {
        return replayCode;
    }

    public void setReplayCode(String replayCode) {
        this.replayCode = replayCode;
    }

    @Basic
    @Column(name = "ANSWER_RRS")
    public String getAnswerRrs() {
        return answerRrs;
    }

    public void setAnswerRrs(String answerRrs) {
        this.answerRrs = answerRrs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DnsTunnelServiceEntity that = (DnsTunnelServiceEntity) o;
        return Objects.equals(alertTime, that.alertTime) &&
                Objects.equals(queryDomainName, that.queryDomainName) &&
                Objects.equals(queryResult, that.queryResult) &&
                Objects.equals(replayCode, that.replayCode) &&
                Objects.equals(answerRrs, that.answerRrs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertTime, queryDomainName, queryResult, replayCode, answerRrs);
    }
}
