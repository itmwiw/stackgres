/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.patroni;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.stackgres.common.ClusterContext;
import io.stackgres.operator.common.StackGresVersion;
import io.stackgres.operator.conciliation.ResourceDiscoverer;

@ApplicationScoped
public class ClusterEnvironmentVariablesFactoryDiscovererImpl
    extends ResourceDiscoverer<ClusterEnvironmentVariablesFactory<ClusterContext>>
    implements ClusterEnvironmentVariablesFactoryDiscoverer<ClusterContext> {

  @Inject
  public ClusterEnvironmentVariablesFactoryDiscovererImpl(
      @Any Instance<ClusterEnvironmentVariablesFactory<ClusterContext>> instance) {
    init(instance);
  }

  @Override
  public List<ClusterEnvironmentVariablesFactory<ClusterContext>> discoverFactories(
      ClusterContext context) {
    StackGresVersion clusterVersion = StackGresVersion.getStackGresVersion(context.getCluster());
    return resourceHub.get(clusterVersion).stream()
        .collect(Collectors.toUnmodifiableList());
  }
}
