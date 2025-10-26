# RestAppGenerator

Initializer Application for Scaffold Spring Boot Project

## Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd rest-app-generator
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

## 🎭 Swagger Endpoint

http://localhost:8080/swagger-ui/index.html#/project-controller/create

## 🎭 Sample YAML For Post Endpoint
```
app:
  name: Demo API
  groupId: com.src.main
  artifactId: demo-api
  version: 0.0.1-SNAPSHOT
  javaVersion: 21
  springBootVersion: 3.3.4
  packaging: jar

dependencies:
  - web
  - validation
  - lombok
  - mapstruct
  - jpa
  - h2
  - openapi

profiles:
  dev:
    properties:
      server.port: 8080
      spring.datasource.url: jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1
      spring.jpa.hibernate.ddl-auto: update
  prod:
    properties:
      server.port: 8080
      spring.jpa.hibernate.ddl-auto: validate

security:
  type: none
  roles:
    - name: ADMIN
    - name: USER
  rules:
    - pattern: /api/admin/**
      methods: [GET, POST, PUT, DELETE]
      roles: [ADMIN]
    - pattern: /api/**
      methods: [GET]
      roles: [USER, ADMIN]
basePackage: com.src.main
dtos:
  - name: AddressDTO
    type: request
    flavor: body
    classConstraints:
      - kind: fieldMatch
        first: password
        second: confirmPassword
        messageKey: "dto.createUser.password.match"
      - kind: conditionalRequired
        field: phone
        dependsOn: contactMethod
        equals: PHONE
        messageKey: "dto.createUser.phone.requiredWhenContactMethodPhone"
      - kind: scriptAssert
        lang: javascript
        script: "_this.startAt == null || _this.expiresAt == null || _this.startAt.isBefore(_this.expiresAt)"
        messageKey: "dto.createUser.time.window"
    fields:
      - name: line1
        type: String
        constraints:
          notBlank: { value: true, messageKey: "dto.address.line1.required" }
          size:     { min: 3, max: 120, messageKey: "dto.address.line1.size" }

      - name: line2
        type: String
        constraints:
          null: { value: true, messageKey: "dto.address.line2.mustBeNull" }

      - name: city
        type: String
        constraints:
          notBlank: { value: true, messageKey: "dto.address.city.required" }
          size:     { min: 2, max: 60, messageKey: "dto.address.city.size" }

      - name: postalCode
        type: String
        constraints:
          notBlank: { value: true, messageKey: "dto.address.postal.required" }
          pattern:  { regex: "^[A-Za-z0-9\\- ]{4,10}$", messageKey: "dto.address.postal.pattern" }

      - name: countryCode
        type: String
        constraints:
          notBlank: { value: true, messageKey: "dto.address.country.required" }
          size:     { min: 2, max: 2, messageKey: "dto.address.country.size" }

  - name: CreateUserRequest
    type: request
    flavor: body
    fields:
      - name: name
        type: String
        jsonProperty: full_name
        constraints:
          notBlank: { value: true, messageKey: "dto.createUser.name.notBlank" }
          size:     { min: 2, max: 80, messageKey: "dto.createUser.name.size" }
      - name: nickname
        type: String
        constraints:
          notEmpty: { value: true, messageKey: "dto.createUser.nickname.notEmpty" }
          size:     { max: 40, messageKey: "dto.createUser.nickname.size" }
      - name: referenceCode
        type: String
        constraints:
          null: { value: true, messageKey: "dto.createUser.reference.mustBeNull" }
      - name: email
        type: String
        jsonProperty: email
        constraints:
          notNull: { value: true, messageKey: "dto.createUser.email.required" }
          email:   { value: true, messageKey: "dto.createUser.email.invalid" }
          size:    { max: 200, messageKey: "dto.createUser.email.size" }
      - name: username
        type: String
        constraints:
          notBlank: { value: true, messageKey: "dto.createUser.username.notBlank" }
          size:     { min: 3, max: 30, messageKey: "dto.createUser.username.size" }
          pattern:  { regex: "^[A-Za-z0-9_]+$", messageKey: "dto.createUser.username.pattern" }
      - name: password
        type: String
        constraints:
          notNull: { value: true, messageKey: "dto.createUser.password.required" }
          size:    { min: 8, max: 128, messageKey: "dto.createUser.password.size" }
          pattern: { regex: "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$", messageKey: "dto.createUser.password.pattern" }
      - name: age
        type: Integer
        constraints:
          notNull: { value: true, messageKey: "dto.createUser.age.required" }
          min:     { value: 18,  messageKey: "dto.createUser.age.min" }
          max:     { value: 120, messageKey: "dto.createUser.age.max" }
          positiveOrZero: { value: true, messageKey: "dto.createUser.age.posOrZero" } # redundant but illustrative
      - name: quantity
        type: Integer
        constraints:
          positive: { value: true, messageKey: "dto.createUser.quantity.positive" }
      - name: delta
        type: Integer
        constraints:
          negativeOrZero: { value: true, messageKey: "dto.createUser.delta.negOrZero" }
      - name: price
        type: BigDecimal
        constraints:
          decimalMin: { value: "0.01", inclusive: true,  messageKey: "dto.createUser.price.decimalMin" }
          decimalMax: { value: "999999.99", inclusive: true, messageKey: "dto.createUser.price.decimalMax" }
          digits:     { integer: 8, fraction: 2, messageKey: "dto.createUser.price.digits" }
      - name: discount
        type: BigDecimal
        constraints:
          negative: { value: true, messageKey: "dto.createUser.discount.negative" }
      - name: birthDate
        type: LocalDate
        constraints:
          past: { value: true, messageKey: "dto.createUser.birthDate.past" }
      - name: createdAt
        type: OffsetDateTime
        constraints:
          pastOrPresent: { value: true, messageKey: "dto.createUser.createdAt.pastOrPresent" }
      - name: startAt
        type: OffsetDateTime
        constraints:
          futureOrPresent: { value: true, messageKey: "dto.createUser.startAt.futureOrPresent" }
      - name: expiresAt
        type: OffsetDateTime
        constraints:
          future: { value: true, messageKey: "dto.createUser.expiresAt.future" }
      - name: agreedTerms
        type: Boolean
        constraints:
          assertTrue: { value: true, messageKey: "dto.createUser.agreedTerms.assertTrue" }
      - name: archived
        type: Boolean
        constraints:
          assertFalse: { value: true, messageKey: "dto.createUser.archived.assertFalse" }
      - name: addresses
        type: "List<AddressDTO>"
        constraints:
          notEmpty: { value: true, messageKey: "dto.createUser.addresses.notEmpty" }
```
