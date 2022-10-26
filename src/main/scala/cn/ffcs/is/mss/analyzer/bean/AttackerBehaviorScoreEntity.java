package cn.ffcs.is.mss.analyzer.bean;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author hanyu
 * @ClassName AttackerBehaviorScoreEntity
 * @date 2022/3/30 15:33
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
@Entity
@Table(name = "attacker_behavior_score", schema = "bigdata", catalog = "")
public class AttackerBehaviorScoreEntity {
    private Timestamp attackerTiame;
    private String attackerUsername;
    private String attackerScore;

    @Basic
    @Column(name = "attacker_tiame")
    public Timestamp getAttackerTiame() {
        return attackerTiame;
    }

    public void setAttackerTiame(Timestamp attackerTiame) {
        this.attackerTiame = attackerTiame;
    }

    @Basic
    @Column(name = "attacker_username")
    public String getAttackerUsername() {
        return attackerUsername;
    }

    public void setAttackerUsername(String attackerUsername) {
        this.attackerUsername = attackerUsername;
    }

    @Basic
    @Column(name = "attacker_score")
    public String getAttackerScore() {
        return attackerScore;
    }

    public void setAttackerScore(String attackerScore) {
        this.attackerScore = attackerScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttackerBehaviorScoreEntity that = (AttackerBehaviorScoreEntity) o;
        return Objects.equals(attackerTiame, that.attackerTiame) && Objects.equals(attackerUsername, that.attackerUsername) && Objects.equals(attackerScore, that.attackerScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attackerTiame, attackerUsername, attackerScore);
    }
}
