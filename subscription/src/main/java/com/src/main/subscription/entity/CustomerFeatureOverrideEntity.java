package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.OverrideType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer_feature_override")
@Getter
@Setter
public class CustomerFeatureOverrideEntity extends BaseSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "feature_id", nullable = false)
	private SubscriptionFeatureEntity feature;

	@Column(name = "is_enabled")
	private Boolean isEnabled;

	@Column(name = "limit_value")
	private Long limitValue;

	@Column(name = "decimal_value", precision = 19, scale = 4)
	private BigDecimal decimalValue;

	@Column(name = "string_value", length = 500)
	private String stringValue;

	@Enumerated(EnumType.STRING)
	@Column(name = "override_type", nullable = false, length = 50)
	private OverrideType overrideType;

	@Column(name = "reason", length = 500)
	private String reason;

	@Column(name = "effective_from", nullable = false)
	private LocalDateTime effectiveFrom;

	@Column(name = "effective_to")
	private LocalDateTime effectiveTo;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;

	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;
}
