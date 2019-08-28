/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.customresources.sgcluster;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresClusterSpec implements KubernetesResource {

  private static final long serialVersionUID = -5276087851826599719L;

  @JsonProperty("instances")
  @Min(1)
  private int instances;

  @JsonProperty("pg_version")
  @Min(value = 11, message = "PostgreSQL version should be at least 11")
  private Integer postgresVersion;

  @JsonProperty("pg_config")
  @NotNull
  private String postgresConfig;

  @JsonProperty("resource_profile")
  @NotNull
  private String resourceProfile;

  @JsonProperty("sidecars")
  private List<String> sidecars;

  public int getInstances() {
    return instances;
  }

  public void setInstances(int instances) {
    this.instances = instances;
  }

  public Integer getPostgresVersion() {
    return postgresVersion;
  }

  public void setPostgresVersion(Integer postgresVersion) {
    this.postgresVersion = postgresVersion;
  }

  public String getPostgresConfig() {
    return postgresConfig;
  }

  public void setPostgresConfig(String postgresConfig) {
    this.postgresConfig = postgresConfig;
  }

  public String getResourceProfile() {
    return resourceProfile;
  }

  public void setResourceProfile(String resourceProfile) {
    this.resourceProfile = resourceProfile;
  }

  public List<String> getSidecars() {
    return sidecars;
  }

  public void setSidecars(List<String> sidecars) {
    this.sidecars = sidecars;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("instances", instances)
        .add("pg_version", postgresVersion)
        .add("pg_config", postgresConfig)
        .add("resource_profile", resourceProfile)
        .toString();
  }

}
