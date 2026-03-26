package com.src.main.dto;

public class InitializrProjectModel {
    private String groupId;
    private String artifactId;
    private String version;
    private String name;
    private String description;
    private String packaging;
    private String generator;
    private String jdkVersion;
    private String bootVersion;
    private boolean includeOpenapi;
    private boolean includeLombok;
    private boolean angularIntegration;

    public String getGroupId() {
        return this.groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public String getVersion() {
        return this.version;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPackaging() {
        return this.packaging;
    }

    public String getGenerator() {
        return this.generator;
    }

    public String getJdkVersion() {
        return this.jdkVersion;
    }

    public String getBootVersion() {
        return this.bootVersion;
    }

    public boolean isIncludeOpenapi() {
        return this.includeOpenapi;
    }

    public boolean isIncludeLombok() {
        return this.includeLombok;
    }

    public boolean isAngularIntegration() {
        return this.angularIntegration;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setPackaging(final String packaging) {
        this.packaging = packaging;
    }

    public void setGenerator(final String generator) {
        this.generator = generator;
    }

    public void setJdkVersion(final String jdkVersion) {
        this.jdkVersion = jdkVersion;
    }

    public void setBootVersion(final String bootVersion) {
        this.bootVersion = bootVersion;
    }

    public void setIncludeOpenapi(final boolean includeOpenapi) {
        this.includeOpenapi = includeOpenapi;
    }

    public void setIncludeLombok(final boolean includeLombok) {
        this.includeLombok = includeLombok;
    }

    public void setAngularIntegration(final boolean angularIntegration) {
        this.angularIntegration = angularIntegration;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof InitializrProjectModel)) return false;
        final InitializrProjectModel other = (InitializrProjectModel) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isIncludeOpenapi() != other.isIncludeOpenapi()) return false;
        if (this.isIncludeLombok() != other.isIncludeLombok()) return false;
        if (this.isAngularIntegration() != other.isAngularIntegration()) return false;
        final Object this$groupId = this.getGroupId();
        final Object other$groupId = other.getGroupId();
        if (this$groupId == null ? other$groupId != null : !this$groupId.equals(other$groupId)) return false;
        final Object this$artifactId = this.getArtifactId();
        final Object other$artifactId = other.getArtifactId();
        if (this$artifactId == null ? other$artifactId != null : !this$artifactId.equals(other$artifactId)) return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
        final Object this$packaging = this.getPackaging();
        final Object other$packaging = other.getPackaging();
        if (this$packaging == null ? other$packaging != null : !this$packaging.equals(other$packaging)) return false;
        final Object this$generator = this.getGenerator();
        final Object other$generator = other.getGenerator();
        if (this$generator == null ? other$generator != null : !this$generator.equals(other$generator)) return false;
        final Object this$jdkVersion = this.getJdkVersion();
        final Object other$jdkVersion = other.getJdkVersion();
        if (this$jdkVersion == null ? other$jdkVersion != null : !this$jdkVersion.equals(other$jdkVersion)) return false;
        final Object this$bootVersion = this.getBootVersion();
        final Object other$bootVersion = other.getBootVersion();
        if (this$bootVersion == null ? other$bootVersion != null : !this$bootVersion.equals(other$bootVersion)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof InitializrProjectModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isIncludeOpenapi() ? 79 : 97);
        result = result * PRIME + (this.isIncludeLombok() ? 79 : 97);
        result = result * PRIME + (this.isAngularIntegration() ? 79 : 97);
        final Object $groupId = this.getGroupId();
        result = result * PRIME + ($groupId == null ? 43 : $groupId.hashCode());
        final Object $artifactId = this.getArtifactId();
        result = result * PRIME + ($artifactId == null ? 43 : $artifactId.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $packaging = this.getPackaging();
        result = result * PRIME + ($packaging == null ? 43 : $packaging.hashCode());
        final Object $generator = this.getGenerator();
        result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
        final Object $jdkVersion = this.getJdkVersion();
        result = result * PRIME + ($jdkVersion == null ? 43 : $jdkVersion.hashCode());
        final Object $bootVersion = this.getBootVersion();
        result = result * PRIME + ($bootVersion == null ? 43 : $bootVersion.hashCode());
        return result;
    }

    public InitializrProjectModel(final String groupId, final String artifactId, final String version, final String name, final String description, final String packaging, final String generator, final String jdkVersion, final String bootVersion, final boolean includeOpenapi, final boolean includeLombok, final boolean angularIntegration) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.packaging = packaging;
        this.generator = generator;
        this.jdkVersion = jdkVersion;
        this.bootVersion = bootVersion;
        this.includeOpenapi = includeOpenapi;
        this.includeLombok = includeLombok;
        this.angularIntegration = angularIntegration;
    }

    public InitializrProjectModel() {
    }

    @Override
    public String toString() {
        return "InitializrProjectModel(groupId=" + this.getGroupId() + ", artifactId=" + this.getArtifactId() + ", version=" + this.getVersion() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", packaging=" + this.getPackaging() + ", generator=" + this.getGenerator() + ", jdkVersion=" + this.getJdkVersion() + ", bootVersion=" + this.getBootVersion() + ", includeOpenapi=" + this.isIncludeOpenapi() + ", includeLombok=" + this.isIncludeLombok() + ", angularIntegration=" + this.isAngularIntegration() + ")";
    }
}
