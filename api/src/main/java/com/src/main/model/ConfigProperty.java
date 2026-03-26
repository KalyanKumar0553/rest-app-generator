package com.src.main.model;

import com.src.main.config.AppDbTables;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = AppDbTables.CONFIG_PROPERTIES)
public class ConfigProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category", nullable = false, length = 100)
    private String category;
    @Column(name = "label", nullable = false, length = 200)
    private String label;
    @Column(name = "property_key", nullable = false, length = 300, unique = true)
    private String propertyKey;
    @Column(name = "current_value_key", length = 200)
    private String currentValueKey;
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<ConfigPropertyValue> allowedValues = new LinkedHashSet<>();

    public void clearAndAddValues(Set<ConfigPropertyValue> newValues) {
        this.allowedValues.clear();
        if (newValues != null) {
            for (ConfigPropertyValue v : newValues) {
                v.setProperty(this);
                this.allowedValues.add(v);
            }
        }
    }

    public Long getId() {
        return this.id;
    }

    public String getCategory() {
        return this.category;
    }

    public String getLabel() {
        return this.label;
    }

    public String getPropertyKey() {
        return this.propertyKey;
    }

    public String getCurrentValueKey() {
        return this.currentValueKey;
    }

    public Set<ConfigPropertyValue> getAllowedValues() {
        return this.allowedValues;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public void setPropertyKey(final String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public void setCurrentValueKey(final String currentValueKey) {
        this.currentValueKey = currentValueKey;
    }

    public void setAllowedValues(final Set<ConfigPropertyValue> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public ConfigProperty(final Long id, final String category, final String label, final String propertyKey, final String currentValueKey, final Set<ConfigPropertyValue> allowedValues) {
        this.id = id;
        this.category = category;
        this.label = label;
        this.propertyKey = propertyKey;
        this.currentValueKey = currentValueKey;
        this.allowedValues = allowedValues;
    }

    public ConfigProperty() {
    }
}
