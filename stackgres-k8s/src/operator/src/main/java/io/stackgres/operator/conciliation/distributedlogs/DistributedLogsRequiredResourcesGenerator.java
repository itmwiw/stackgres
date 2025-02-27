/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.distributedlogs;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import io.stackgres.common.resource.ResourceFinder;
import io.stackgres.operator.conciliation.RequiredResourceGenerator;
import io.stackgres.operator.conciliation.ResourceGenerationDiscoverer;
import io.stackgres.operator.conciliation.factory.DecoratorDiscoverer;

@ApplicationScoped
public class DistributedLogsRequiredResourcesGenerator
    implements RequiredResourceGenerator<StackGresDistributedLogs> {

  private final ResourceGenerationDiscoverer<StackGresDistributedLogsContext> generators;

  private final DecoratorDiscoverer<StackGresDistributedLogsContext> decoratorDiscoverer;

  private final ConnectedClustersScanner connectedClustersScanner;

  private final ResourceFinder<Secret> secretFinder;

  @Inject
  public DistributedLogsRequiredResourcesGenerator(
      ResourceGenerationDiscoverer<StackGresDistributedLogsContext> generators,
      DecoratorDiscoverer<StackGresDistributedLogsContext> decoratorDiscoverer,
      ConnectedClustersScanner connectedClustersScanner,
      ResourceFinder<Secret> secretFinder) {
    this.generators = generators;
    this.decoratorDiscoverer = decoratorDiscoverer;
    this.connectedClustersScanner = connectedClustersScanner;
    this.secretFinder = secretFinder;
  }

  @Override
  public List<HasMetadata> getRequiredResources(StackGresDistributedLogs config) {
    final String distributedLogsName = config.getMetadata().getName();
    final String namespace = config.getMetadata().getNamespace();
    StackGresDistributedLogsContext context = ImmutableStackGresDistributedLogsContext.builder()
        .source(config)
        .addAllConnectedClusters(getConnectedClusters(config))
        .databaseCredentials(secretFinder.findByNameAndNamespace(distributedLogsName, namespace))
        .build();

    final List<HasMetadata> requiredResources = generators.getResourceGenerators(context)
        .stream().flatMap(generator -> generator.generateResource(context))
        .collect(Collectors.toUnmodifiableList());

    var decorators = decoratorDiscoverer.discoverDecorator(context);

    decorators.forEach(decorator -> decorator.decorate(context, requiredResources));

    return requiredResources;
  }

  private List<StackGresCluster> getConnectedClusters(
      StackGresDistributedLogs distributedLogs) {
    return connectedClustersScanner.getConnectedClusters(distributedLogs);
  }
}
