/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.operator.conciliation.ResourceDiscoverer;
import io.stackgres.operator.conciliation.cluster.StackGresVersion;
import io.stackgres.operator.conciliation.factory.Decorator;

@ApplicationScoped
public class DecoratorDiscovererImpl
    extends ResourceDiscoverer<Decorator<StackGresCluster>>
    implements DecoratorDiscoverer<StackGresCluster> {

  @Inject
  public DecoratorDiscovererImpl(
      @Any Instance<Decorator<StackGresCluster>> instance) {
    init(instance);

  }

  @Override
  public List<Decorator<StackGresCluster>> discoverDecorator(StackGresCluster context) {

    StackGresVersion version = StackGresVersion.getClusterStackGresVersion(context);
    return resourceHub.get(version).stream()
        .collect(Collectors.toUnmodifiableList());

  }
}
