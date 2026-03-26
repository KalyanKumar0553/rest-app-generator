package com.src.main.model;

import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.CONFIG_PROPERTY_VALUES)
public class ConfigPropertyValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private ConfigProperty property;
    @Column(name = "value_key", nullable = false, length = 200)
    private String valueKey;
    @Column(name = "value_label", nullable = false, length = 200)
    private String valueLabel;


    public static class ConfigPropertyValueBuilder {
        private Long id;
        private ConfigProperty property;
        private String valueKey;
        private String valueLabel;

        ConfigPropertyValueBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ConfigPropertyValue.ConfigPropertyValueBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ConfigPropertyValue.ConfigPropertyValueBuilder property(final ConfigProperty property) {
            this.property = property;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ConfigPropertyValue.ConfigPropertyValueBuilder valueKey(final String valueKey) {
            this.valueKey = valueKey;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ConfigPropertyValue.ConfigPropertyValueBuilder valueLabel(final String valueLabel) {
            this.valueLabel = valueLabel;
            return this;
        }

        public ConfigPropertyValue build() {
            return new ConfigPropertyValue(this.id, this.property, this.valueKey, this.valueLabel);
        }

        @Override
        public String toString() {
            return "ConfigPropertyValue.ConfigPropertyValueBuilder(id=" + this.id + ", property=" + this.property + ", valueKey=" + this.valueKey + ", valueLabel=" + this.valueLabel + ")";
        }
    }

    public static ConfigPropertyValue.ConfigPropertyValueBuilder builder() {
        return new ConfigPropertyValue.ConfigPropertyValueBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public ConfigProperty getProperty() {
        return this.property;
    }

    public String getValueKey() {
        return this.valueKey;
    }

    public String getValueLabel() {
        return this.valueLabel;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setProperty(final ConfigProperty property) {
        this.property = property;
    }

    public void setValueKey(final String valueKey) {
        this.valueKey = valueKey;
    }

    public void setValueLabel(final String valueLabel) {
        this.valueLabel = valueLabel;
    }

    public ConfigPropertyValue(final Long id, final ConfigProperty property, final String valueKey, final String valueLabel) {
        this.id = id;
        this.property = property;
        this.valueKey = valueKey;
        this.valueLabel = valueLabel;
    }

    public ConfigPropertyValue() {
    }
}
