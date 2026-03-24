package com.src.main.subscription.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "plan_feature_mapping")
@Getter
@Setter
public class PlanFeatureMappingEntity extends BaseSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlanEntity plan;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "feature_id", nullable = false)
	private SubscriptionFeatureEntity feature;

	@Column(name = "is_enabled", nullable = false)
	private Boolean isEnabled = Boolean.FALSE;

	@Column(name = "limit_value")
	private Long limitValue;

	@Column(name = "decimal_value", precision = 19, scale = 4)
	private BigDecimal decimalValue;

	@Column(name = "string_value", length = 500)
	private String stringValue;

	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;
}
