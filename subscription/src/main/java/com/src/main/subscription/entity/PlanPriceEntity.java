package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.BillingCycle;

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
@Table(name = "plan_price")
@Getter
@Setter
public class PlanPriceEntity extends BaseSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlanEntity plan;

	@Enumerated(EnumType.STRING)
	@Column(name = "billing_cycle", nullable = false, length = 50)
	private BillingCycle billingCycle;

	@Column(name = "currency_code", nullable = false, length = 10)
	private String currencyCode;

	@Column(name = "amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "discount_percent", precision = 5, scale = 2)
	private BigDecimal discountPercent;

	@Column(name = "effective_from", nullable = false)
	private LocalDateTime effectiveFrom;

	@Column(name = "effective_to")
	private LocalDateTime effectiveTo;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;

	@Column(name = "display_label", length = 100)
	private String displayLabel;

	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;
}
