/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.distributedlogs.patroni;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.kubernetes.api.model.EndpointsBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.stackgres.common.LabelFactoryForCluster;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import io.stackgres.operator.common.StackGresVersion;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.ResourceGenerator;
import io.stackgres.operator.conciliation.distributedlogs.StackGresDistributedLogsContext;

@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V09, stopAt = StackGresVersion.V10)
public class PatroniFailover implements ResourceGenerator<StackGresDistributedLogsContext> {

  private final LabelFactoryForCluster<StackGresDistributedLogs> labelFactory;

  @Inject
  public PatroniFailover(LabelFactoryForCluster<StackGresDistributedLogs> labelFactory) {
    this.labelFactory = labelFactory;
  }

  private static String name(StackGresDistributedLogs cluster) {
    return cluster.getMetadata().getName() + "-failover";
  }

  @Override
  public Stream<HasMetadata> generateResource(StackGresDistributedLogsContext context) {
    StackGresDistributedLogs cluster = context.getSource();
    return Stream.of(
        new EndpointsBuilder()
            .withNewMetadata()
            .withNamespace(cluster.getMetadata().getNamespace())
            .withName(name(cluster))
            .withLabels(labelFactory.patroniClusterLabels(cluster))
            .endMetadata()
            .build()
    );
  }
}
