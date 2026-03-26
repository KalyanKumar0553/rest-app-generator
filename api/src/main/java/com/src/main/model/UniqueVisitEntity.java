package com.src.main.model;

import java.time.OffsetDateTime;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.UNIQUE_VISITS)
public class UniqueVisitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ip_hash", nullable = false, unique = true, length = 64)
    private String ipHash;
    @Column(name = "first_seen_at", nullable = false)
    private OffsetDateTime firstSeenAt;
    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;
    @Column(name = "hit_count", nullable = false)
    private long hitCount;

    public UniqueVisitEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public String getIpHash() {
        return this.ipHash;
    }

    public OffsetDateTime getFirstSeenAt() {
        return this.firstSeenAt;
    }

    public OffsetDateTime getLastSeenAt() {
        return this.lastSeenAt;
    }

    public long getHitCount() {
        return this.hitCount;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setIpHash(final String ipHash) {
        this.ipHash = ipHash;
    }

    public void setFirstSeenAt(final OffsetDateTime firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public void setLastSeenAt(final OffsetDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void setHitCount(final long hitCount) {
        this.hitCount = hitCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UniqueVisitEntity)) return false;
        final UniqueVisitEntity other = (UniqueVisitEntity) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getHitCount() != other.getHitCount()) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$ipHash = this.getIpHash();
        final Object other$ipHash = other.getIpHash();
        if (this$ipHash == null ? other$ipHash != null : !this$ipHash.equals(other$ipHash)) return false;
        final Object this$firstSeenAt = this.getFirstSeenAt();
        final Object other$firstSeenAt = other.getFirstSeenAt();
        if (this$firstSeenAt == null ? other$firstSeenAt != null : !this$firstSeenAt.equals(other$firstSeenAt)) return false;
        final Object this$lastSeenAt = this.getLastSeenAt();
        final Object other$lastSeenAt = other.getLastSeenAt();
        if (this$lastSeenAt == null ? other$lastSeenAt != null : !this$lastSeenAt.equals(other$lastSeenAt)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UniqueVisitEntity;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $hitCount = this.getHitCount();
        result = result * PRIME + (int) ($hitCount >>> 32 ^ $hitCount);
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $ipHash = this.getIpHash();
        result = result * PRIME + ($ipHash == null ? 43 : $ipHash.hashCode());
        final Object $firstSeenAt = this.getFirstSeenAt();
        result = result * PRIME + ($firstSeenAt == null ? 43 : $firstSeenAt.hashCode());
        final Object $lastSeenAt = this.getLastSeenAt();
        result = result * PRIME + ($lastSeenAt == null ? 43 : $lastSeenAt.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "UniqueVisitEntity(id=" + this.getId() + ", ipHash=" + this.getIpHash() + ", firstSeenAt=" + this.getFirstSeenAt() + ", lastSeenAt=" + this.getLastSeenAt() + ", hitCount=" + this.getHitCount() + ")";
    }
}
