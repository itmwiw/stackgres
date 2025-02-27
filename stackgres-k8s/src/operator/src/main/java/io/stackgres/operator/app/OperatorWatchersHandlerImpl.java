/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.CustomResourceList;
import io.fabric8.kubernetes.client.Watcher.Action;
import io.quarkus.runtime.Application;
import io.stackgres.common.KubernetesClientFactory;
import io.stackgres.common.crd.sgbackup.StackGresBackup;
import io.stackgres.common.crd.sgbackup.StackGresBackupList;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfig;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfigList;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterList;
import io.stackgres.common.crd.sgdbops.StackGresDbOps;
import io.stackgres.common.crd.sgdbops.StackGresDbOpsList;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogsList;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfig;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfigList;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfig;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfigList;
import io.stackgres.common.crd.sgprofile.StackGresProfile;
import io.stackgres.common.crd.sgprofile.StackGresProfileList;
import io.stackgres.operator.conciliation.cluster.ClusterReconciliator;
import io.stackgres.operator.conciliation.dbops.DbOpsReconciliator;
import io.stackgres.operator.conciliation.distributedlogs.DistributedLogsReconciliator;
import io.stackgres.operator.controller.ResourceWatcherFactory;
import io.stackgres.operatorframework.resource.WatcherMonitor;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class OperatorWatchersHandlerImpl implements OperatorWatcherHandler {

  private final List<WatcherMonitor<?>> monitors = new ArrayList<>();

  private final KubernetesClientFactory kubeClient;
  private final ClusterReconciliator clusterReconciliationCycle;
  private final DistributedLogsReconciliator distributedLogsReconciliatorCycle;
  private final DbOpsReconciliator dbOpsReconciliationCycle;
  private final ResourceWatcherFactory watcherFactory;

  @Inject
  public OperatorWatchersHandlerImpl(KubernetesClientFactory kubeClient,
      ClusterReconciliator clusterReconciliationCycle,
      DistributedLogsReconciliator distributedLogsReconciliatorCycle,
      DbOpsReconciliator dbOpsReconciliationCycle, ResourceWatcherFactory watcherFactory) {
    this.kubeClient = kubeClient;
    this.clusterReconciliationCycle = clusterReconciliationCycle;
    this.distributedLogsReconciliatorCycle = distributedLogsReconciliatorCycle;
    this.dbOpsReconciliationCycle = dbOpsReconciliationCycle;
    this.watcherFactory = watcherFactory;
  }

  @Override
  public void startWatchers() {
    monitors.add(createWatcher(
        StackGresCluster.class,
        StackGresClusterList.class,
        reconcileCluster()
            .andThen(reconcileDistributedLogs())));

    monitors.add(createWatcher(
        StackGresPostgresConfig.class,
        StackGresPostgresConfigList.class,
        reconcileCluster()));

    monitors.add(createWatcher(
        StackGresPoolingConfig.class,
        StackGresPoolingConfigList.class,
        reconcileCluster()));

    monitors.add(createWatcher(
        StackGresProfile.class,
        StackGresProfileList.class,
        reconcileCluster()));

    monitors.add(createWatcher(
        StackGresBackupConfig.class,
        StackGresBackupConfigList.class,
        reconcileCluster()));

    monitors.add(createWatcher(
        StackGresBackup.class,
        StackGresBackupList.class,
        reconcileCluster()));

    monitors.add(createWatcher(
        StackGresDbOps.class,
        StackGresDbOpsList.class,
        reconcileDbOps()));

    monitors.add(createWatcher(
        StackGresDistributedLogs.class,
        StackGresDistributedLogsList.class,
        reconcileDistributedLogs()));

  }

  private <T extends CustomResource<?, ?>,
      L extends CustomResourceList<T>> WatcherMonitor<T> createWatcher(
      @NotNull Class<T> crClass, @NotNull Class<L> listClass, @NotNull Consumer<Action> consumer) {

    return new WatcherMonitor<>(watcherListener -> kubeClient.create()
        .customResources(crClass, listClass)
        .inAnyNamespace()
        .watch(watcherFactory.createWatcher(consumer, watcherListener)),
        () -> new Thread(() -> Application.currentApplication().stop()).start());
  }

  private Consumer<Action> reconcileCluster() {
    return action -> clusterReconciliationCycle.reconcile();
  }

  private Consumer<Action> reconcileDistributedLogs() {
    return action -> distributedLogsReconciliatorCycle.reconcile();
  }

  private Consumer<Action> reconcileDbOps() {
    return action -> dbOpsReconciliationCycle.reconcile();
  }

  @Override
  public void stopWatchers() {
    monitors.forEach(WatcherMonitor::close);
  }

}
