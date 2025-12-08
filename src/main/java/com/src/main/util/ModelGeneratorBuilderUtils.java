package com.src.main.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.Strings;

import com.src.main.dto.ColumnSpecDTO;
import com.src.main.dto.ConstraintDTO;
import com.src.main.dto.FieldBlockDTO;
import com.src.main.dto.FieldNameAndType;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.GenerationSpecDTO;
import com.src.main.dto.IdBlockDTO;
import com.src.main.dto.JoinColumnSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.RelationBlockDTO;
import com.src.main.dto.RelationSpecDTO;

public final class ModelGeneratorBuilderUtils {

	private ModelGeneratorBuilderUtils() {
	}

	public static IdBlockDTO buildIdBlock(ModelSpecDTO m, Set<String> imports) {
		Validate.notNull(m.getId(), "entity.id missing for %s", m.getName());

		FieldNameAndType nat = normalizeNameType(m.getId().getField(), m.getId().getType());
		IdBlockDTO id = new IdBlockDTO();
		id.setName(nat.name());
		id.setCapitalizedName(CaseUtils.toPascal(nat.name())); // <--- added
		id.setType(resolveJavaType(nat.type(), imports));

		List<String> ann = new ArrayList<>();

		// Always @Id
		imports.add("jakarta.persistence.Id");
		ann.add("@Id");

		GenerationSpecDTO g = m.getId().getGeneration();
		if (g != null && g.getStrategy() != null) {
			imports.add("jakarta.persistence.GeneratedValue");

			switch (g.getStrategy()) {
			case IDENTITY -> {
				imports.add("jakarta.persistence.GenerationType");
				ann.add("@GeneratedValue(strategy = GenerationType.IDENTITY)");
			}
			case SEQUENCE -> {
				imports.add("jakarta.persistence.GenerationType");
				imports.add("jakarta.persistence.SequenceGenerator");

				String genName = defaultSeqName(m);
				String seqName = StringUtils.firstNonBlank(g.getSequenceName(), genName);
				Integer allocationSize = g.getAllocationSize();
				int alloc = allocationSize != null ? allocationSize : 50;

				ann.add("@SequenceGenerator(name = \"" + genName + "\", sequenceName = \"" + seqName
						+ "\", allocationSize = " + alloc + ")");
				ann.add("@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"" + genName + "\")");
			}
			case UUID -> {
				// Default to Hibernate @GenericGenerator("uuid","uuid2") for compatibility
				imports.add("org.hibernate.annotations.GenericGenerator");

				String generatorName = StringUtils.firstNonBlank(g.getGeneratorName(), "uuid");
				String strategy = StringUtils.firstNonBlank(g.getHibernateUuidStrategy(), "uuid2");

				ann.add("@GenericGenerator(name = \"" + generatorName + "\", strategy = \"" + strategy + "\")");
				ann.add("@GeneratedValue(generator = \"" + generatorName + "\")");
			}
			case AUTO -> {
				imports.add("jakarta.persistence.GenerationType");
				ann.add("@GeneratedValue(strategy = GenerationType.AUTO)");
			}
			case NONE -> {
				// no-op: manually assigned IDs
			}
			}
		}

		id.setAnnotations(ann);
		return id;
	}

	public static FieldBlockDTO buildFieldBlock(ModelSpecDTO m, FieldSpecDTO f, Set<String> imports) {
		FieldNameAndType nat = normalizeNameType(f.getName(), f.getType());
		FieldBlockDTO fb = new FieldBlockDTO();
		fb.setName(nat.name());
		fb.setCapitalizedName(CaseUtils.toPascal(nat.name())); // <--- added
		String resolvedType = resolveJavaType(nat.type(), imports);
		fb.setType(resolvedType);

		List<String> ann = new ArrayList<>();

		// @Column (from explicit column or inferred from constraints)
		ColumnSpecDTO col = f.getColumn();
		boolean hasExplicitColumn = col != null && (col.getName() != null || col.getLength() != null
				|| col.getNullable() != null || col.getUnique() != null || col.getColumnDefinition() != null);

		// Bean Validation (unchanged)
		if (f.getConstraints() != null) {
			for (ConstraintDTO c : f.getConstraints()) {
				final String name = c.getName();
				if (name == null)
					continue;

				switch (name) {
				case "NotNull" -> {
					imports.add("jakarta.validation.constraints.NotNull");
					ann.add(ModelGeneratorUtils.msgAnno("@NotNull", m, f, "NotNull"));
					col = ModelGeneratorUtils.ensureColumn(col);
					col.setNullable(Boolean.FALSE);
				}
				case "NotBlank" -> {
					imports.add("jakarta.validation.constraints.NotBlank");
					ann.add(ModelGeneratorUtils.msgAnno("@NotBlank", m, f, "NotBlank"));
				}
				case "Email" -> {
					imports.add("jakarta.validation.constraints.Email");
					ann.add(ModelGeneratorUtils.msgAnno("@Email", m, f, "Email"));
				}
				case "Pattern" -> {
					imports.add("jakarta.validation.constraints.Pattern");
					String re = ModelGeneratorUtils.getStringAny(c.getParams(), new String[] { "regex", "regexp" },
							".*");
					ann.add(ModelGeneratorUtils.msgAnno(
							"@Pattern(regexp = \"" + ModelGeneratorUtils.escapeJava(re) + "\")", m, f, "Pattern"));
				}
				case "Size" -> {
					imports.add("jakarta.validation.constraints.Size");
					Integer min = ModelGeneratorUtils.getInt(c.getParams(), "min", null);
					Integer max = ModelGeneratorUtils.getInt(c.getParams(), "max", null);

					String args = (min != null ? "min=" + min : "") + ((min != null && max != null) ? ", " : "")
							+ (max != null ? "max=" + max : "");
					ann.add(ModelGeneratorUtils.msgAnno("@Size(" + args + ")", m, f, "Size"));

					if (!hasExplicitColumn && "String".equals(resolvedType) && max != null) {
						col = ModelGeneratorUtils.ensureColumn(col);
						if (col.getLength() == null) {
							col.setLength(max);
						}
					}
				}
				case "Min" -> {
					imports.add("jakarta.validation.constraints.Min");
					long val = ModelGeneratorUtils.getLong(c.getParams(), "value", 0L);
					String msgKey = ModelGeneratorUtils.buildMessageKey(m, f, "Min");
					ann.add(ModelGeneratorUtils.buildMinAnnotation(val, msgKey));
				}
				case "Max" -> {
					imports.add("jakarta.validation.constraints.Max");
					long val = ModelGeneratorUtils.getLong(c.getParams(), "value", Long.MAX_VALUE);
					String msgKey = ModelGeneratorUtils.buildMessageKey(m, f, "Max");
					ann.add(ModelGeneratorUtils.buildMaxAnnotation(val, msgKey));
				}
				case "Positive" -> {
					imports.add("jakarta.validation.constraints.Positive");
					ann.add(ModelGeneratorUtils.msgAnno("@Positive", m, f, "Positive"));
				}
				case "DecimalMin" -> {
					imports.add("jakarta.validation.constraints.DecimalMin");
					String val = ModelGeneratorUtils.getString(c.getParams(), "value", "0");
					boolean inclusive = ModelGeneratorUtils.getBoolean(c.getParams(), "inclusive", true);
					ann.add(ModelGeneratorUtils.msgAnno("@DecimalMin(value=\"" + ModelGeneratorUtils.escapeJava(val)
							+ "\", inclusive=" + inclusive + ")", m, f, "DecimalMin"));
				}
				case "Digits" -> {
					imports.add("jakarta.validation.constraints.Digits");
					int integer = ModelGeneratorUtils.getInt(c.getParams(), "integer", 10);
					int fraction = ModelGeneratorUtils.getInt(c.getParams(), "fraction", 2);
					ann.add(ModelGeneratorUtils.msgAnno("@Digits(integer=" + integer + ", fraction=" + fraction + ")",
							m, f, "Digits"));
				}
				case "Past" -> {
					imports.add("jakarta.validation.constraints.Past");
					ann.add(ModelGeneratorUtils.msgAnno("@Past", m, f, "Past"));
				}
				case "Future" -> {
					imports.add("jakarta.validation.constraints.Future");
					ann.add(ModelGeneratorUtils.msgAnno("@Future", m, f, "Future"));
				}
				case "PastOrPresent" -> {
					imports.add("jakarta.validation.constraints.PastOrPresent");
					ann.add(ModelGeneratorUtils.msgAnno("@PastOrPresent", m, f, "PastOrPresent"));
				}
				case "AssertTrue" -> {
					imports.add("jakarta.validation.constraints.AssertTrue");
					ann.add(ModelGeneratorUtils.msgAnno("@AssertTrue", m, f, "AssertTrue"));
				}
				case "AssertFalse" -> {
					imports.add("jakarta.validation.constraints.AssertFalse");
					ann.add(ModelGeneratorUtils.msgAnno("@AssertFalse", m, f, "AssertFalse"));
				}
				case "Valid" -> {
					imports.add("jakarta.validation.Valid");
					ann.add("@Valid");
				}
				default -> {
					// ignore unknowns here
				}
				}
			}
		}

		// @Convert (explicit only)
		if (f.getJpa() != null && f.getJpa().getConvert() != null
				&& Strings.isNotBlank(f.getJpa().getConvert().getConverter())) {
			imports.add("jakarta.persistence.Convert");
			ann.add("@Convert(converter = " + f.getJpa().getConvert().getConverter() + ".class)");
		}

		// @NaturalId
		if (Boolean.TRUE.equals(f.getNaturalId())) {
			imports.add("org.hibernate.annotations.NaturalId");
			ann.add("@NaturalId");
		}

		// @Column
		if (col != null && (hasExplicitColumn || col.getNullable() != null || col.getUnique() != null
				|| col.getLength() != null || col.getColumnDefinition() != null || col.getName() != null)) {

			imports.add("jakarta.persistence.Column");
			StringBuilder sb = new StringBuilder("@Column(");
			List<String> args = new ArrayList<>();
			if (col.getName() != null)
				args.add("name = \"" + col.getName() + "\"");
			if (col.getUnique() != null)
				args.add("unique = " + col.getUnique());
			if (col.getNullable() != null)
				args.add("nullable = " + col.getNullable());
			if (col.getLength() != null)
				args.add("length = " + col.getLength());
			if (col.getColumnDefinition() != null)
				args.add("columnDefinition = \"" + ModelGeneratorUtils.escapeJava(col.getColumnDefinition()) + "\"");
			sb.append(String.join(", ", args)).append(")");
			ann.add(sb.toString());
		}

		fb.setAnnotations(ann);
		return fb;
	}

	public static RelationBlockDTO buildRelationBlock(ModelSpecDTO m, RelationSpecDTO r, Set<String> imports) {
		RelationBlockDTO rb = new RelationBlockDTO();
		String fieldName = CaseUtils.toCamel(r.getName());
		rb.setName(fieldName);
		rb.setCapitalizedName(CaseUtils.toPascal(fieldName)); // <--- added

		String targetType = CaseUtils.toPascal(r.getTarget());
		String fieldType;
		List<String> ann = new ArrayList<>();

		switch (r.getType()) {
		case OneToMany -> {
			imports.add("jakarta.persistence.OneToMany");
			imports.add("jakarta.persistence.FetchType");
			String mappedBy = Validate.notBlank(r.getMappedBy(), "OneToMany mappedBy required on %s.%s", m.getName(),
					r.getName());
			String cascade = ModelGeneratorUtils.toCascadeArray(r.getCascade());
			ann.add("@OneToMany(mappedBy = \"" + mappedBy + "\", fetch = FetchType.LAZY" + cascade
					+ (Boolean.TRUE.equals(r.getOrphanRemoval()) ? ", orphanRemoval = true" : "") + ")");
			if (Strings.isNotBlank(r.getOrderBy())) {
				imports.add("jakarta.persistence.OrderBy");
				ann.add("@OrderBy(\"" + r.getOrderBy().trim() + "\")");
			}
			if (r.getOrderColumn() != null && Strings.isNotBlank(r.getOrderColumn().getName())) {
				imports.add("jakarta.persistence.OrderColumn");
				ann.add("@OrderColumn(name = \"" + r.getOrderColumn().getName() + "\")");
			}
			fieldType = "List<" + targetType + ">";
			imports.add("java.util.List");
		}
		case ManyToOne -> {
			imports.add("jakarta.persistence.ManyToOne");
			imports.add("jakarta.persistence.FetchType");
			String optional = r.getOptional() == null ? "true" : r.getOptional().toString();
			ann.add("@ManyToOne(fetch = FetchType.LAZY, optional = " + optional + ")");
			JoinColumnSpecDTO jc = r.getJoinColumn();
			if (jc != null) {
				imports.add("jakarta.persistence.JoinColumn");
				StringBuilder s = new StringBuilder("@JoinColumn(");
				List<String> args = new ArrayList<>();
				if (Strings.isNotBlank(jc.getName()))
					args.add("name = \"" + jc.getName() + "\"");
				if (jc.getNullable() != null)
					args.add("nullable = " + jc.getNullable());
				if (Strings.isNotBlank(jc.getReferencedColumnName()))
					args.add("referencedColumnName = \"" + jc.getReferencedColumnName() + "\"");
				s.append(String.join(", ", args)).append(")");
				ann.add(s.toString());
			}
			fieldType = targetType;
		}
		case ManyToMany -> {
			imports.add("jakarta.persistence.ManyToMany");
			imports.add("jakarta.persistence.FetchType");
			String cascade = ModelGeneratorUtils.toCascadeArray(r.getCascade());
			ann.add("@ManyToMany(fetch = FetchType.LAZY" + cascade + ")");
			if (r.getJoinTable() != null) {
				imports.add("jakarta.persistence.JoinTable");
				imports.add("jakarta.persistence.JoinColumn");
				String jt = ModelGeneratorUtils.joinTableAnnotation(r.getJoinTable());
				ann.add(jt);
			}
			fieldType = "Set<" + targetType + ">";
			imports.add("java.util.Set");
		}
		default -> throw new IllegalStateException("Unsupported relation: " + r.getType());
		}

		rb.setAnnotations(ann);
		rb.setDeclarationType(fieldType);
		rb.setTargetType(targetType);
		return rb;
	}

	private static String resolveJavaType(String rawType, Set<String> imports) {
		if (rawType == null) {
			return null;
		}

		String type = rawType.trim();

		// 1) Handle generics: List<Address>, Map<String, Integer> etc.
		if (type.contains("<") && type.contains(">")) {
			String outer = type.substring(0, type.indexOf("<")).trim();
			String inner = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();

			// Import outer generic type
			addImportIfNeeded(outer, imports);

			// Recursively resolve inner type(s)
			String resolvedInner;
			if (inner.contains(",")) {
				// Map<K,V>
				String[] parts = inner.split(",");
				resolvedInner = Arrays.stream(parts).map(String::trim).map(t -> resolveJavaType(t, imports))
						.collect(Collectors.joining(", "));
			} else {
				resolvedInner = resolveJavaType(inner, imports);
			}

			return outer + "<" + resolvedInner + ">";
		}

		// 2) Non-generic simple types
		return switch (type) {
		case "String" -> {
			imports.add("java.lang.String");
			yield "String";
		}
		case "Integer" -> {
			imports.add("java.lang.Integer");
			yield "Integer";
		}
		case "Long" -> {
			imports.add("java.lang.Long");
			yield "Long";
		}
		case "Boolean" -> {
			imports.add("java.lang.Boolean");
			yield "Boolean";
		}
		case "BigDecimal" -> {
			imports.add("java.math.BigDecimal");
			yield "BigDecimal";
		}
		case "UUID" -> {
			imports.add("java.util.UUID");
			yield "UUID";
		}
		case "LocalDate" -> {
			imports.add("java.time.LocalDate");
			yield "LocalDate";
		}
		case "LocalDateTime" -> {
			imports.add("java.time.LocalDateTime");
			yield "LocalDateTime";
		}
		case "OffsetDateTime" -> {
			imports.add("java.time.OffsetDateTime");
			yield "OffsetDateTime";
		}
		default -> {
			// 3) Custom classes or fully-qualified types
			if (type.contains(".")) {
				imports.add(type);
				yield type.substring(type.lastIndexOf('.') + 1);
			}
			yield type;
		}
		};
	}

	private static FieldNameAndType normalizeNameType(String rawName, String rawType) {
		String name = CaseUtils.toCamel(rawName);
		String type = StringUtils.firstNonBlank(rawType, "String");
		return new FieldNameAndType(name, type);
	}

	private static void addImportIfNeeded(String outer, Set<String> imports) {
		switch (outer) {
		case "List" -> imports.add("java.util.List");
		case "Set" -> imports.add("java.util.Set");
		case "Map" -> imports.add("java.util.Map");
		default -> {
			if (outer.contains(".")) {
				imports.add(outer);
			}
		}
		}
	}

	private static String defaultSeqName(ModelSpecDTO m) {
		return CaseUtils.toSnake(m.getName()) + "_seq";
	}

}
