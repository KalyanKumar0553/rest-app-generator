package com.src.main.subscription.entity;

import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.enums.ResetPolicy;
import com.src.main.subscription.enums.ValueDataType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_feature")
@Getter
@Setter
public class SubscriptionFeatureEntity extends BaseSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, length = 100, unique = true)
	private String code;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description", length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "feature_type", nullable = false, length = 50)
	private FeatureType featureType;

	@Enumerated(EnumType.STRING)
	@Column(name = "value_data_type", length = 50)
	private ValueDataType valueDataType;

	@Column(name = "unit", length = 50)
	private String unit;

	@Enumerated(EnumType.STRING)
	@Column(name = "reset_policy", length = 50)
	private ResetPolicy resetPolicy;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;

	@Column(name = "is_system", nullable = false)
	private Boolean isSystem = Boolean.FALSE;

	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;
}
