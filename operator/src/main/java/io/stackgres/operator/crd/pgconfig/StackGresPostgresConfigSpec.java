/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.crd.pgconfig;

import java.util.Map;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresPostgresConfigSpec implements KubernetesResource<StackGresPostgresConfig> {

  private static final long serialVersionUID = -5276087851826599719L;

  @JsonProperty("pg_version")
  @Min(11)
  private int pgVersion;

  @JsonProperty("postgresql.conf")
  @NotEmpty
  private Map<String, String> postgresqlConf;

  public int getPgVersion() {
    return pgVersion;
  }

  public void setPgVersion(int pgVersion) {
    this.pgVersion = pgVersion;
  }

  public Map<String, String> getPostgresqlConf() {
    return postgresqlConf;
  }

  public void setPostgresqlConf(Map<String, String> postgresqlConf) {
    this.postgresqlConf = postgresqlConf;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("pg_version", pgVersion)
        .add("postgresql.conf", postgresqlConf)
        .toString();
  }

}
