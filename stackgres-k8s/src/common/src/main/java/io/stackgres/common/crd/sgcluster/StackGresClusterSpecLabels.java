/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresClusterSpecLabels {

  private Map<String, String> clusterPods;

  public Map<String, String> getClusterPods() {
    return clusterPods;
  }

  public void setClusterPods(Map<String, String> clusterPods) {
    this.clusterPods = clusterPods;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterPods);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresClusterSpecLabels)) {
      return false;
    }
    StackGresClusterSpecLabels other = (StackGresClusterSpecLabels) obj;
    return Objects.equals(clusterPods, other.clusterPods);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}
