/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresClusterSpec implements KubernetesResource {

  private static final long serialVersionUID = -5276087851826599719L;

  @JsonProperty("instances")
  @Positive(message = "You need at least 1 instance in the cluster")
  private int instances;

  @JsonProperty("postgresVersion")
  @NotBlank(message = "PostgreSQL version is required")
  private String postgresVersion;

  @JsonProperty("configurations")
  @NotNull(message = "cluster configuration cannot be null")
  @Valid
  private StackGresClusterConfiguration configuration;

  @JsonProperty("sgInstanceProfile")
  @NotNull(message = "resource profile must not be null")
  private String resourceProfile;

  @JsonProperty("initialData")
  @Valid
  private StackGresClusterInitData initData;

  @JsonProperty("pods")
  @NotNull(message = "pod description must be specified")
  @Valid
  private StackGresClusterPod pod;

  @JsonProperty("distributedLogs")
  @Valid
  private StackGresClusterDistributedLogs distributedLogs;

  @JsonProperty("postgresExtensions")
  @Valid
  private List<StackGresClusterExtension> postgresExtensions;

  @JsonProperty("prometheusAutobind")
  private Boolean prometheusAutobind;

  @JsonProperty("nonProductionOptions")
  @Valid
  private StackGresClusterNonProduction nonProduction;

  @JsonProperty("postgresServices")
  @Valid
  private StackGresClusterPostgresServices postgresServices;

  @JsonProperty("metadata")
  @Valid
  private StackGresClusterSpecMetadata metadata;

  @JsonProperty("postgres")
  @Valid
  private StackGresClusterPostgres postgres;

  public int getInstances() {
    return instances;
  }

  public void setInstances(int instances) {
    this.instances = instances;
  }

  public String getPostgresVersion() {
    return postgresVersion;
  }

  public void setPostgresVersion(String postgresVersion) {
    this.postgresVersion = postgresVersion;
  }

  public StackGresClusterConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(StackGresClusterConfiguration configuration) {
    this.configuration = configuration;
  }

  public String getResourceProfile() {
    return resourceProfile;
  }

  public void setResourceProfile(String resourceProfile) {
    this.resourceProfile = resourceProfile;
  }

  public StackGresClusterInitData getInitData() {
    return initData;
  }

  public void setInitData(StackGresClusterInitData initData) {
    this.initData = initData;
  }

  public StackGresClusterPod getPod() {
    return pod;
  }

  public void setPod(StackGresClusterPod pod) {
    this.pod = pod;
  }

  public StackGresClusterDistributedLogs getDistributedLogs() {
    return distributedLogs;
  }

  public void setDistributedLogs(StackGresClusterDistributedLogs distributedLogs) {
    this.distributedLogs = distributedLogs;
  }

  public List<StackGresClusterExtension> getPostgresExtensions() {
    return postgresExtensions;
  }

  public void setPostgresExtensions(List<StackGresClusterExtension> postgresExtensions) {
    this.postgresExtensions = postgresExtensions;
  }

  public Boolean getPrometheusAutobind() {
    return prometheusAutobind;
  }

  public void setPrometheusAutobind(Boolean prometheusAutobind) {
    this.prometheusAutobind = prometheusAutobind;
  }

  public StackGresClusterNonProduction getNonProduction() {
    return nonProduction;
  }

  public void setNonProduction(StackGresClusterNonProduction nonProduction) {
    this.nonProduction = nonProduction;
  }

  public StackGresClusterPostgresServices getPostgresServices() {
    return postgresServices;
  }

  public void setPostgresServices(StackGresClusterPostgresServices postgresServices) {
    this.postgresServices = postgresServices;
  }

  public StackGresClusterSpecMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(StackGresClusterSpecMetadata metadata) {
    this.metadata = metadata;
  }

  public StackGresClusterPostgres getPostgres() {
    return postgres;
  }

  public void setPostgres(StackGresClusterPostgres postgres) {
    this.postgres = postgres;
  }

  @Override
  public int hashCode() {
    return Objects.hash(configuration, distributedLogs, initData, instances, metadata,
        nonProduction, pod, postgresExtensions, postgresServices, postgresVersion,
        prometheusAutobind, resourceProfile, postgres);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresClusterSpec)) {
      return false;
    }
    StackGresClusterSpec other = (StackGresClusterSpec) obj;
    return Objects.equals(configuration, other.configuration)
        && Objects.equals(distributedLogs, other.distributedLogs)
        && Objects.equals(initData, other.initData) && instances == other.instances
        && Objects.equals(metadata, other.metadata)
        && Objects.equals(nonProduction, other.nonProduction) && Objects.equals(pod, other.pod)
        && Objects.equals(postgresExtensions, other.postgresExtensions)
        && Objects.equals(postgresServices, other.postgresServices)
        && Objects.equals(postgresVersion, other.postgresVersion)
        && Objects.equals(prometheusAutobind, other.prometheusAutobind)
        && Objects.equals(resourceProfile, other.resourceProfile)
        && Objects.equals(postgres, other.postgres);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}
