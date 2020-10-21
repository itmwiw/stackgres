/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.patroni;

import static io.stackgres.operator.conciliation.factory.cluster.patroni.PatroniSecret.AUTHENTICATOR_PASSWORD_KEY;
import static io.stackgres.operator.conciliation.factory.cluster.patroni.PatroniSecret.REPLICATION_PASSWORD_KEY;
import static io.stackgres.operator.conciliation.factory.cluster.patroni.PatroniSecret.SUPERUSER_PASSWORD_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.stackgres.common.LabelFactory;
import io.stackgres.common.StringUtil;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.resource.ResourceFinder;
import io.stackgres.testutil.JsonUtil;
import io.stackgres.operator.common.LabelFactoryDelegator;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatroniSecretTest {

  private final Secret existentSecret = new SecretBuilder()
      .addToData(ImmutableMap.of(
          SUPERUSER_PASSWORD_KEY, StringUtil.generateRandom(),
          REPLICATION_PASSWORD_KEY, StringUtil.generateRandom(),
          AUTHENTICATOR_PASSWORD_KEY, StringUtil.generateRandom()))
      .build();

  @Mock
  private LabelFactoryDelegator delegator;

  @Mock
  private LabelFactory<StackGresCluster> labelFactory;

  @Mock
  private ResourceFinder<Secret> secretFinder;

  @Mock
  private StackGresClusterContext generatorContext;

  private final PatroniSecret patroniSecret = new PatroniSecret();

  @BeforeEach
  void setUp() {
    StackGresCluster defaultCluster = JsonUtil
        .readFromJson("stackgres_cluster/default.json", StackGresCluster.class);
    patroniSecret.setFactoryFactory(labelFactory);
    patroniSecret.setSecretFinder(secretFinder);
    when(labelFactory.clusterLabels(any(StackGresCluster.class))).thenReturn(ImmutableMap.of());

    when(generatorContext.getSource()).thenReturn(defaultCluster);

  }

  @Test
  void generateResources_shouldGenerateRandomPasswords() {

    when(secretFinder.findByNameAndNamespace(anyString(), anyString()))
        .thenReturn(Optional.empty());

    Stream<HasMetadata> resourcesStream = patroniSecret.generateResource(generatorContext);
    List<HasMetadata> resources = resourcesStream.collect(Collectors.toUnmodifiableList());
    assertEquals(1, resources.size());

    assertEquals(Secret.class, resources.get(0).getClass());

    Secret secret = (Secret) resources.get(0);

    final Map<String, String> data = secret.getData();
    assertTrue(data.containsKey(SUPERUSER_PASSWORD_KEY));
    assertTrue(data.containsKey(REPLICATION_PASSWORD_KEY));
    assertTrue(data.containsKey(AUTHENTICATOR_PASSWORD_KEY));

    final Map<String, String> existentData = existentSecret.getData();
    assertNotEquals(existentData.get(SUPERUSER_PASSWORD_KEY), data.get(SUPERUSER_PASSWORD_KEY));
    assertNotEquals(existentData.get(REPLICATION_PASSWORD_KEY), data.get(REPLICATION_PASSWORD_KEY));
    assertNotEquals(existentData.get(AUTHENTICATOR_PASSWORD_KEY), data.get(AUTHENTICATOR_PASSWORD_KEY));

  }

  @Test
  void generateResources_shouldNotGenerateRandomPasswords() {

    when(secretFinder.findByNameAndNamespace(anyString(), anyString()))
        .thenReturn(Optional.of(existentSecret));

    Stream<HasMetadata> resourcesStream = patroniSecret.generateResource(generatorContext);
    List<HasMetadata> resources = resourcesStream.collect(Collectors.toUnmodifiableList());
    assertEquals(1, resources.size());

    assertEquals(Secret.class, resources.get(0).getClass());

    Secret secret = (Secret) resources.get(0);

    final Map<String, String> data = secret.getData();
    assertTrue(data.containsKey(SUPERUSER_PASSWORD_KEY));
    assertTrue(data.containsKey(REPLICATION_PASSWORD_KEY));
    assertTrue(data.containsKey(AUTHENTICATOR_PASSWORD_KEY));

    final Map<String, String> existentData = existentSecret.getData();
    assertEquals(existentData.get(SUPERUSER_PASSWORD_KEY), data.get(SUPERUSER_PASSWORD_KEY));
    assertEquals(existentData.get(REPLICATION_PASSWORD_KEY), data.get(REPLICATION_PASSWORD_KEY));
    assertEquals(existentData.get(AUTHENTICATOR_PASSWORD_KEY), data.get(AUTHENTICATOR_PASSWORD_KEY));

  }
}