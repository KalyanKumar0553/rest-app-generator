INSERT INTO config_property (category, label, property_key) VALUES
('MODEL_ANNOTATIONS', 'Model Class-level JPA Annotations',       'MODEL_ANNOTATIONS_CLASS_JPA'),
('MODEL_ANNOTATIONS', 'Model Class-level Hibernate Annotations', 'MODEL_ANNOTATIONS_CLASS_HIBERNATE'),
('MODEL_ANNOTATIONS', 'Model ID Annotations',                    'MODEL_ANNOTATIONS_ID'),
('MODEL_ANNOTATIONS', 'Model Field/Relation JPA Annotations',    'MODEL_ANNOTATIONS_FIELD_JPA'),
('MODEL_ANNOTATIONS', 'Model Field-level Hibernate Annotations', 'MODEL_ANNOTATIONS_FIELD_HIBERNATE'),
('MODEL_ANNOTATIONS', 'Model Validation Annotations',            'MODEL_ANNOTATIONS_VALIDATION'),
('MODEL_ANNOTATIONS', 'Model Lombok Annotations',                'MODEL_ANNOTATIONS_LOMBOK'),
('MODEL_ANNOTATIONS', 'Model Auditing Annotations',              'MODEL_ANNOTATIONS_AUDITING');
-- ===== CLASS-LEVEL JPA =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Entity', '@Entity'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_CLASS_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Table', '@Table'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_CLASS_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'UniqueConstraint', '@UniqueConstraint'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_CLASS_JPA';
-- ===== CLASS-LEVEL HIBERNATE =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Immutable', '@Immutable'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_CLASS_HIBERNATE';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'NaturalIdCache', '@NaturalIdCache'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_CLASS_HIBERNATE';
-- ===== ID ANNOTATIONS =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Id', '@Id'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_ID';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'GeneratedValue', '@GeneratedValue'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_ID';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'SequenceGenerator', '@SequenceGenerator'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_ID';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'GenericGenerator', '@GenericGenerator'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_ID';
-- ===== FIELD / RELATION JPA =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Column', '@Column'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Convert', '@Convert'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'OneToMany', '@OneToMany'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'OrderBy', '@OrderBy'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'OrderColumn', '@OrderColumn'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'ManyToOne', '@ManyToOne'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'ManyToMany', '@ManyToMany'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'JoinColumn', '@JoinColumn'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'JoinTable', '@JoinTable'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_JPA';
-- ===== FIELD-LEVEL HIBERNATE =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'NaturalId', '@NaturalId'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_FIELD_HIBERNATE';
-- ===== VALIDATION ANNOTATIONS =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'NotNull', '@NotNull'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'NotBlank', '@NotBlank'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Email', '@Email'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Pattern', '@Pattern'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Size', '@Size'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Min', '@Min'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Max', '@Max'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Positive', '@Positive'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'DecimalMin', '@DecimalMin'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Digits', '@Digits'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Past', '@Past'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Future', '@Future'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'PastOrPresent', '@PastOrPresent'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'AssertTrue', '@AssertTrue'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'AssertFalse', '@AssertFalse'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Valid', '@Valid'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_VALIDATION';
-- ===== LOMBOK ANNOTATIONS =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Getter', '@Getter'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_LOMBOK';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Setter', '@Setter'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_LOMBOK';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'Builder', '@Builder'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_LOMBOK';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'ToString', '@ToString'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_LOMBOK';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'EqualsAndHashCode', '@EqualsAndHashCode'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_LOMBOK';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'NoArgsConstructor', '@NoArgsConstructor'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_LOMBOK';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'AllArgsConstructor', '@AllArgsConstructor'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_LOMBOK';
-- ===== AUDITING ANNOTATIONS =====
INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'CreatedDate', '@CreatedDate'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_AUDITING';

INSERT INTO config_property_values (property_id, value_key, value_label)
SELECT p.id, 'LastModifiedDate', '@LastModifiedDate'
FROM config_property p WHERE p.property_key = 'MODEL_ANNOTATIONS_AUDITING';
