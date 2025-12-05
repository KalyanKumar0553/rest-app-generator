package com.src.main.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "config_property")
@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
