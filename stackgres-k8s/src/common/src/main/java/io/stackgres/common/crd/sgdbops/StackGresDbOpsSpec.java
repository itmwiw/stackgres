/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgdbops;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.validation.FieldReference;
import io.stackgres.common.validation.FieldReference.ReferencedField;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresDbOpsSpec implements KubernetesResource {

  private static final long serialVersionUID = 1L;

  @JsonProperty("sgCluster")
  @NotNull(message = "sgCluster must be provided")
  private String sgCluster;

  @JsonProperty("op")
  @NotNull(message = "op must be provided")
  private String op;

  @JsonProperty("runAt")
  private String runAt;

  @JsonProperty("timeout")
  private String timeout;

  @JsonProperty("maxRetries")
  @Min(value = 0, message = "maxRetries must be greather or equals to 0.")
  @Max(value = 10, message = "maxRetries must be less or equals to 10.")
  private Integer maxRetries;

  @JsonProperty("benchmark")
  @Valid
  private StackGresDbOpsBenchmark benchmark;

  @JsonProperty("vacuum")
  @Valid
  private StackGresDbOpsVacuum vacuum;

  @JsonProperty("repack")
  @Valid
  private StackGresDbOpsRepack repack;

  @JsonProperty("majorVersionUpgrade")
  @Valid
  private StackGresDbOpsMajorVersionUpgrade majorVersionUpgrade;

  @JsonProperty("restart")
  @Valid
  private StackGresDbOpsRestart restart;

  @JsonProperty("minorVersionUpgrade")
  @Valid
  private StackGresDbOpsMinorVersionUpgrade minorVersionUpgrade;

  @JsonProperty("securityUpgrade")
  @Valid
  private StackGresDbOpsSecurityUpgrade securityUpgrade;

  @ReferencedField("op")
  interface Op extends FieldReference { }

  @ReferencedField("runAt")
  interface RunAt extends FieldReference { }

  @ReferencedField("timeout")
  interface Timeout extends FieldReference { }

  @ReferencedField("benchmark")
  interface Benchmark extends FieldReference { }

  @ReferencedField("minorVersionUpgrade")
  interface MinorVersionUpgrade extends FieldReference { }

  @ReferencedField("majorVersionUpgrade")
  interface MajorVersionUpgrade extends FieldReference { }

  @JsonIgnore
  @AssertTrue(message = "op must be one of benchmark, vacuum, repack,"
      + " majorVersionUpgrade, restart, minorVersionUpgrade or securityUpgrade",
      payload = Op.class)
  public boolean isOpValid() {
    return op == null || ImmutableList.of(
        "benchmark",
        "vacuum",
        "repack",
        "majorVersionUpgrade",
        "restart",
        "minorVersionUpgrade",
        "securityUpgrade"
        ).contains(op);
  }

  @JsonIgnore
  @AssertTrue(message = "runAt must be in ISO 8601 date format: `YYYY-MM-DDThh:mm:ss.ddZ`.",
      payload = RunAt.class)
  public boolean isRunAtValid() {
    try {
      if (runAt != null) {
        Instant.parse(runAt);
      }
      return true;
    } catch (DateTimeParseException ex) {
      return false;
    }
  }

  @JsonIgnore
  @AssertTrue(message = "timeout must be positive and in ISO 8601 duration format:"
      + " `PnDTnHnMn.nS`.",
      payload = Timeout.class)
  public boolean isTimeoutValid() {
    try {
      if (timeout != null) {
        return !Duration.parse(timeout).isNegative();
      }
      return true;
    } catch (DateTimeParseException ex) {
      return false;
    }
  }

  @JsonIgnore
  @AssertTrue(message = "benchmark section must be provided.",
      payload = Benchmark.class)
  public boolean isBenchmarkSectionProvided() {
    return !Objects.equals(op, "benchmark") || benchmark != null;
  }

  @JsonIgnore
  @AssertTrue(message = "minorVersionUpgrade section must be provided.",
      payload = MinorVersionUpgrade.class)
  public boolean isMinorVersionUpgradeSectionProvided() {
    return !Objects.equals(op, "minorVersionUpgrade") || minorVersionUpgrade != null;
  }

  @JsonIgnore
  @AssertTrue(message = "majorVersionUpgrade section must be provided.",
      payload = MajorVersionUpgrade.class)
  public boolean isMajorVersionUpgradeSectionProvided() {
    return !Objects.equals(op, "majorVersionUpgrade") || majorVersionUpgrade != null;
  }

  @JsonIgnore
  public boolean isOpBenchmark() {
    return Objects.equals(op, "benchmark");
  }

  @JsonIgnore
  public boolean isOpVacuum() {
    return Objects.equals(op, "vacuum");
  }

  @JsonIgnore
  public boolean isOpRepack() {
    return Objects.equals(op, "repack");
  }

  @JsonIgnore
  public boolean isOpMajorVersionUpgrade() {
    return Objects.equals(op, "majorVersionUpgrade");
  }

  @JsonIgnore
  public boolean isOpRestart() {
    return Objects.equals(op, "restart");
  }

  @JsonIgnore
  public boolean isOpMinorVersionUpgrade() {
    return Objects.equals(op, "minorVersionUpgrade");
  }

  @JsonIgnore
  public boolean isOpSecurityUpgrade() {
    return Objects.equals(op, "securityUpgrade");
  }

  public String getSgCluster() {
    return sgCluster;
  }

  public void setSgCluster(String sgCluster) {
    this.sgCluster = sgCluster;
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public String getRunAt() {
    return runAt;
  }

  public void setRunAt(String runAt) {
    this.runAt = runAt;
  }

  public String getTimeout() {
    return timeout;
  }

  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }

  public Integer getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(Integer maxRetries) {
    this.maxRetries = maxRetries;
  }

  public StackGresDbOpsBenchmark getBenchmark() {
    return benchmark;
  }

  public void setBenchmark(StackGresDbOpsBenchmark benchmark) {
    this.benchmark = benchmark;
  }

  public StackGresDbOpsVacuum getVacuum() {
    return vacuum;
  }

  public void setVacuum(StackGresDbOpsVacuum vacuum) {
    this.vacuum = vacuum;
  }

  public StackGresDbOpsRepack getRepack() {
    return repack;
  }

  public void setRepack(StackGresDbOpsRepack repack) {
    this.repack = repack;
  }

  public StackGresDbOpsMajorVersionUpgrade getMajorVersionUpgrade() {
    return majorVersionUpgrade;
  }

  public void setMajorVersionUpgrade(StackGresDbOpsMajorVersionUpgrade majorVersionUpgrade) {
    this.majorVersionUpgrade = majorVersionUpgrade;
  }

  public StackGresDbOpsRestart getRestart() {
    return restart;
  }

  public void setRestart(StackGresDbOpsRestart restart) {
    this.restart = restart;
  }

  public StackGresDbOpsMinorVersionUpgrade getMinorVersionUpgrade() {
    return minorVersionUpgrade;
  }

  public void setMinorVersionUpgrade(StackGresDbOpsMinorVersionUpgrade minorVersionUpgrade) {
    this.minorVersionUpgrade = minorVersionUpgrade;
  }

  public StackGresDbOpsSecurityUpgrade getSecurityUpgrade() {
    return securityUpgrade;
  }

  public void setSecurityUpgrade(StackGresDbOpsSecurityUpgrade securityUpgrade) {
    this.securityUpgrade = securityUpgrade;
  }

  @Override
  public int hashCode() {
    return Objects.hash(benchmark, majorVersionUpgrade, maxRetries, minorVersionUpgrade, op, repack,
        restart, runAt, securityUpgrade, sgCluster, timeout, vacuum);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresDbOpsSpec)) {
      return false;
    }
    StackGresDbOpsSpec other = (StackGresDbOpsSpec) obj;
    return Objects.equals(benchmark, other.benchmark)
        && Objects.equals(majorVersionUpgrade, other.majorVersionUpgrade)
        && Objects.equals(maxRetries, other.maxRetries)
        && Objects.equals(minorVersionUpgrade, other.minorVersionUpgrade)
        && Objects.equals(op, other.op) && Objects.equals(repack, other.repack)
        && Objects.equals(restart, other.restart) && Objects.equals(runAt, other.runAt)
        && Objects.equals(securityUpgrade, other.securityUpgrade)
        && Objects.equals(sgCluster, other.sgCluster) && Objects.equals(timeout, other.timeout)
        && Objects.equals(vacuum, other.vacuum);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}
