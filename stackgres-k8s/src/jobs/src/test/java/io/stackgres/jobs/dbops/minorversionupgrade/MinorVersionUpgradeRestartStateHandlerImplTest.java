/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.dbops.minorversionupgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.stackgres.common.crd.sgcluster.ClusterDbOpsRestartStatus;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterDbOpsMinorVersionUpgradeStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterDbOpsStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterStatus;
import io.stackgres.common.crd.sgdbops.DbOpsRestartStatus;
import io.stackgres.common.crd.sgdbops.StackGresDbOps;
import io.stackgres.common.crd.sgdbops.StackGresDbOpsMinorVersionUpgradeStatus;
import io.stackgres.jobs.dbops.AbstractRestartStateHandler;
import io.stackgres.jobs.dbops.ClusterStateHandlerTest;
import io.stackgres.jobs.dbops.StateHandler;
import io.stackgres.jobs.dbops.clusterrestart.ImmutablePatroniInformation;
import io.stackgres.jobs.dbops.clusterrestart.MemberRole;
import io.stackgres.jobs.dbops.clusterrestart.MemberState;
import io.stackgres.jobs.dbops.clusterrestart.PatroniApiHandler;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
class MinorVersionUpgradeRestartStateHandlerImplTest extends ClusterStateHandlerTest {

  @Inject
  @StateHandler("minorVersionUpgrade")
  MinorVersionUpgradeRestartStateHandlerImpl restartStateHandler;

  @InjectMock
  PatroniApiHandler patroniApi;

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
    lenient().when(patroniApi.getMembersPatroniInformation(anyString(), anyString()))
        .thenReturn(Uni.createFrom().item(
            List.of(
                ImmutablePatroniInformation.builder()
                    .state(MemberState.RUNNING)
                    .role(MemberRole.LEADER)
                    .serverVersion(110005)
                    .patroniVersion("1.6.5")
                    .patroniScope(clusterName)
                    .build(),
                ImmutablePatroniInformation.builder()
                    .state(MemberState.RUNNING)
                    .role(MemberRole.REPlICA)
                    .serverVersion(110005)
                    .patroniVersion("1.6.5")
                    .patroniScope(clusterName)
                    .build())));
  }

  @Override
  public AbstractRestartStateHandler getRestartStateHandler() {
    return restartStateHandler;
  }

  @Override
  protected StackGresDbOps getDbOps() {
    return JsonUtil.readFromJson("stackgres_dbops/dbops_minorversionupgrade.json",
        StackGresDbOps.class);
  }

  @Override
  protected String getRestartMethod(StackGresDbOps dbOps) {
    return dbOps.getSpec().getMinorVersionUpgrade().getMethod();
  }

  @Override
  public DbOpsRestartStatus getRestartStatus(StackGresDbOps dbOps) {
    return dbOps.getStatus().getMinorVersionUpgrade();
  }

  @Override
  public Optional<ClusterDbOpsRestartStatus> getRestartStatus(StackGresCluster cluster) {
    return Optional.of(cluster)
        .map(StackGresCluster::getStatus)
        .map(StackGresClusterStatus::getDbOps)
        .map(StackGresClusterDbOpsStatus::getMinorVersionUpgrade);
  }

  @Override
  protected void initializeDbOpsStatus(StackGresDbOps dbOps, StackGresCluster cluster,
      List<Pod> pods) {
    final StackGresDbOpsMinorVersionUpgradeStatus minorVersionUpgradeStatus =
        new StackGresDbOpsMinorVersionUpgradeStatus();
    minorVersionUpgradeStatus.setInitialInstances(
        pods.stream()
            .map(Pod::getMetadata).map(ObjectMeta::getName)
            .collect(Collectors.toList()));
    minorVersionUpgradeStatus.setPrimaryInstance(getPrimaryInstance(pods).getMetadata().getName());
    minorVersionUpgradeStatus.setPendingToRestartInstances(
        pods.stream()
            .map(Pod::getMetadata).map(ObjectMeta::getName)
            .collect(Collectors.toList()));
    minorVersionUpgradeStatus.setSwitchoverInitiated(null);
    minorVersionUpgradeStatus.setSourcePostgresVersion(
        cluster.getSpec().getPostgres().getVersion());
    minorVersionUpgradeStatus.setTargetPostgresVersion(
        dbOps.getSpec().getMinorVersionUpgrade().getPostgresVersion());

    dbOps.getStatus().setMinorVersionUpgrade(minorVersionUpgradeStatus);
  }

  @Override
  protected void initializeClusterStatus(StackGresDbOps dbOps, StackGresCluster cluster,
      List<Pod> pods) {
    final StackGresClusterStatus status = new StackGresClusterStatus();
    final StackGresClusterDbOpsStatus dbOpsStatus = new StackGresClusterDbOpsStatus();
    final StackGresClusterDbOpsMinorVersionUpgradeStatus minorVersionUpgradeStatus =
        new StackGresClusterDbOpsMinorVersionUpgradeStatus();
    minorVersionUpgradeStatus.setInitialInstances(
        pods.stream()
            .map(Pod::getMetadata).map(ObjectMeta::getName)
            .collect(Collectors.toList()));
    minorVersionUpgradeStatus.setPrimaryInstance(getPrimaryInstance(pods).getMetadata().getName());
    minorVersionUpgradeStatus.setSourcePostgresVersion(
        cluster.getSpec().getPostgres().getVersion());
    minorVersionUpgradeStatus.setTargetPostgresVersion(
        dbOps.getSpec().getMinorVersionUpgrade().getPostgresVersion());
    dbOpsStatus.setMinorVersionUpgrade(minorVersionUpgradeStatus);
    status.setDbOps(dbOpsStatus);
    cluster.setStatus(status);

  }

  @Override
  protected void verifyClusterInitializedStatus(List<Pod> pods, StackGresDbOps dbOps,
      StackGresCluster cluster) {
    super.verifyClusterInitializedStatus(pods, dbOps, cluster);
    var restartStatus = cluster.getStatus().getDbOps().getMinorVersionUpgrade();
    assertEquals(dbOps.getStatus().getMinorVersionUpgrade().getTargetPostgresVersion(),
        restartStatus.getTargetPostgresVersion());
    assertEquals(dbOps.getStatus().getMinorVersionUpgrade().getSourcePostgresVersion(),
        cluster.getStatus().getDbOps().getMinorVersionUpgrade().getSourcePostgresVersion());
  }
}
