/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.patroni;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.stackgres.common.ClusterStatefulSetEnvVars;
import io.stackgres.common.StringUtil;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfig;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfig;
import io.stackgres.common.patroni.PatroniConfig;
import io.stackgres.testutil.JsonUtil;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import io.stackgres.operator.conciliation.factory.cluster.patroni.parameters.Blocklist;
import io.stackgres.operator.conciliation.factory.cluster.patroni.parameters.DefaultValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatroniConfigEndpointsTest {

  private static final JsonMapper MAPPER = new JsonMapper();

  @Mock
  private StackGresClusterContext context;

  private PatroniConfigEndpoints generator;

  private StackGresCluster cluster;

  private StackGresBackupConfig backupConfig;

  private StackGresPostgresConfig postgresConfig;

  private String clusterName, clusterNamespace;

  @BeforeEach
  void setUp() {
    generator = new PatroniConfigEndpoints(
        MAPPER);


    cluster = JsonUtil
        .readFromJson("stackgres_cluster/default.json", StackGresCluster.class);
    backupConfig = JsonUtil.readFromJson("backup_config/default.json", StackGresBackupConfig.class);
    postgresConfig = JsonUtil.readFromJson("postgres_config/default_postgres.json", StackGresPostgresConfig.class);

    clusterName = cluster.getMetadata().getName();
    clusterNamespace = cluster.getMetadata().getNamespace();

    lenient().when(context.getBackupConfig()).thenReturn(Optional.of(backupConfig));
    lenient().when(context.getPostgresConfig()).thenReturn(postgresConfig);
    lenient().when(context.getPatroniClusterLabels()).thenReturn(Map.of());
  }

  @Test
  void getPostgresConfigValues_shouldConfigureBackupParametersIfArePresent() {
    when(context.getBackupConfig()).thenReturn(Optional.of(backupConfig));
    when(context.getPostgresConfig()).thenReturn(postgresConfig);


    Map<String, String> pgParams = generator.getPostgresConfigValues(context);

    assertTrue(pgParams.containsKey("archive_command"));
    final String expected = "exec-with-env '" + ClusterStatefulSetEnvVars.BACKUP_ENV.value(cluster) + "'"
        + " -- wal-g wal-push %p";
    assertEquals(expected, pgParams.get("archive_command"));
  }

  @Test
  void getPostgresConfigValues_shouldNotConfigureBackupParametersIfAreNotPresent() {
    when(context.getBackupConfig()).thenReturn(Optional.empty());
    when(context.getPostgresConfig()).thenReturn(postgresConfig);

    Map<String, String> pgParams = generator.getPostgresConfigValues(context);

    assertTrue(pgParams.containsKey("archive_command"));
    assertEquals("/bin/true", pgParams.get("archive_command"));

  }

  @Test
  void getPostgresConfigValues_shouldConfigurePgParameters() {
    when(context.getBackupConfig()).thenReturn(Optional.of(backupConfig));
    when(context.getPostgresConfig()).thenReturn(postgresConfig);

    Map<String, String> pgParams = generator.getPostgresConfigValues(context);

    postgresConfig.getSpec().getPostgresqlConf().forEach((key, value) -> {
      assertTrue(pgParams.containsKey(key));
      assertEquals(value, pgParams.get(key));
    });
  }

  @Test
  void getPostgresConfigValues_shouldNotModifyBlockedValuesIfArePresent() {
    when(context.getBackupConfig()).thenReturn(Optional.empty());
    when(context.getPostgresConfig()).thenReturn(postgresConfig);

    Map<String, String> defValues = DefaultValues.getDefaultValues();

    defValues.forEach((key, value) -> {
      postgresConfig.getSpec().getPostgresqlConf().put(key, StringUtil.generateRandom());
    });

    Map<String, String> pgParams = generator.getPostgresConfigValues(context);

    List<String> blocklistedKeys = Blocklist.getBlocklistParameters();
    defValues.forEach((key, value) -> {
      assertTrue(pgParams.containsKey(key));
      if (blocklistedKeys.contains(key)) {
        assertEquals(value, pgParams.get(key));
      }
    });
  }

  @Test
  void generateResource_shouldSetLabelsFromLabelFactory() {

    final Map<String, String> labels = Map
        .of(StringUtil.generateRandom(), StringUtil.generateRandom());
    when(context.getPatroniClusterLabels()).thenReturn(labels);

    Endpoints endpoints = generateEndpoint();

    assertEquals(labels, endpoints.getMetadata().getLabels());

  }

  @Test
  void generatedEndpoint_shouldBeAnnotatedWithPatroniKeyAndAValidPostgresConfig() throws JsonProcessingException {

    when(context.getPatroniClusterLabels()).thenReturn(Map.of());

    Endpoints endpoints = generateEndpoint();

    final Map<String, String> annotations = endpoints.getMetadata().getAnnotations();
    assertTrue(annotations.containsKey(PatroniConfigEndpoints.PATRONI_CONFIG_KEY));

    PatroniConfig patroniConfig = MAPPER
        .readValue(annotations.get(PatroniConfigEndpoints.PATRONI_CONFIG_KEY), PatroniConfig.class);
    DefaultValues.getDefaultValues().forEach((key, value) ->
        assertTrue(patroniConfig.getPostgresql().getParameters().containsKey(key)));
    assertEquals(30, patroniConfig.getTtl());
    assertEquals(10, patroniConfig.getLoopWait());
    assertEquals(10, patroniConfig.getRetryTimeout());
    assertTrue(patroniConfig.getPostgresql().getUsePgRewind());
    assertNull(patroniConfig.getPostgresql().getUseSlots());

  }

  private Endpoints generateEndpoint() {
    when(context.getSource()).thenReturn(cluster);
    when(context.getBackupConfig()).thenReturn(Optional.of(backupConfig));
    when(context.getPostgresConfig()).thenReturn(postgresConfig);

    List<HasMetadata> endpoints = generator.generateResource(context)
        .collect(Collectors.toUnmodifiableList());

    assertFalse(endpoints.isEmpty());

    final Endpoints endpoint = (Endpoints) endpoints.get(0);
    assertNotNull(endpoint.getMetadata());
    assertNotNull(endpoint.getMetadata().getLabels());
    assertNotNull(endpoint.getMetadata().getAnnotations());
    return endpoint;
  }

}
