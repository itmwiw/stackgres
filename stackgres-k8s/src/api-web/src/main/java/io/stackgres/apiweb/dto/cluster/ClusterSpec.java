/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.dto.cluster;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class ClusterSpec {

  @JsonProperty("postgres")
  private ClusterPostgres postgres;

  @JsonProperty("instances")
  private int instances;

  @JsonProperty("configurations")
  private ClusterConfiguration configurations;

  @JsonProperty("sgInstanceProfile")
  private String sgInstanceProfile;

  @JsonProperty("initialData")
  private ClusterInitData initData;

  @JsonProperty("distributedLogs")
  private ClusterDistributedLogs distributedLogs;

  @JsonProperty("toInstallPostgresExtensions")
  private List<ClusterInstalledExtension> toInstallPostgresExtensions;

  @JsonProperty("pods")
  private ClusterPod pods;

  @JsonProperty("prometheusAutobind")
  private Boolean prometheusAutobind;

  @JsonProperty("nonProductionOptions")
  private ClusterNonProduction nonProduction;

  @JsonProperty("postgresServices")
  private ClusterPostgresServices postgresServices;

  @JsonProperty("metadata")
  private ClusterSpecMetadata metadata;

  public ClusterPostgres getPostgres() {
    return postgres;
  }

  public void setPostgres(ClusterPostgres postgres) {
    this.postgres = postgres;
  }

  public int getInstances() {
    return instances;
  }

  public void setInstances(int instances) {
    this.instances = instances;
  }

  public ClusterConfiguration getConfigurations() {
    return configurations;
  }

  public void setConfigurations(ClusterConfiguration configurations) {
    this.configurations = configurations;
  }

  public String getSgInstanceProfile() {
    return sgInstanceProfile;
  }

  public void setSgInstanceProfile(String sgInstanceProfile) {
    this.sgInstanceProfile = sgInstanceProfile;
  }

  public Boolean getPrometheusAutobind() {
    return prometheusAutobind;
  }

  public void setPrometheusAutobind(Boolean prometheusAutobind) {
    this.prometheusAutobind = prometheusAutobind;
  }

  public ClusterNonProduction getNonProduction() {
    return nonProduction;
  }

  public void setNonProduction(ClusterNonProduction nonProduction) {
    this.nonProduction = nonProduction;
  }

  public ClusterPod getPods() {
    return pods;
  }

  public void setPods(ClusterPod pods) {
    this.pods = pods;
  }

  public ClusterInitData getInitData() {
    return initData;
  }

  public void setInitData(ClusterInitData initData) {
    this.initData = initData;
  }

  public ClusterDistributedLogs getDistributedLogs() {
    return distributedLogs;
  }

  public void setDistributedLogs(ClusterDistributedLogs distributedLogs) {
    this.distributedLogs = distributedLogs;
  }

  public List<ClusterInstalledExtension> getToInstallPostgresExtensions() {
    return toInstallPostgresExtensions;
  }

  public void setToInstallPostgresExtensions(
      List<ClusterInstalledExtension> toInstallPostgresExtensions) {
    this.toInstallPostgresExtensions = toInstallPostgresExtensions;
  }

  public ClusterPostgresServices getPostgresServices() {
    return postgresServices;
  }

  public void setPostgresServices(ClusterPostgresServices postgresServices) {
    this.postgresServices = postgresServices;
  }

  public ClusterSpecMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(ClusterSpecMetadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}
