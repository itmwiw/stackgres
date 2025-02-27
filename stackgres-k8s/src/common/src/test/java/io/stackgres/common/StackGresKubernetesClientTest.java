/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.batch.v1beta1.CronJobBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.stackgres.testutil.StringUtils;
import org.junit.jupiter.api.Test;

@QuarkusTest
@WithKubernetesTestServer
class StackGresKubernetesClientTest {

  String randomFieldManager = StringUtils.getRandomString(6);
  String randomNamespace = StringUtils.getRandomNamespace();
  String randomName = StringUtils.getRandomClusterName();

  @Test
  void givenARole_ShouldProduceAValidUrl() throws MalformedURLException {

    final StackGresDefaultKubernetesClient stackGresKubernetesClient
        = new StackGresDefaultKubernetesClient();
    var actual = stackGresKubernetesClient.getResourceUrl(new PatchContext.Builder()
            .withFieldManager(randomFieldManager).withForce(true).build(),
        new RoleBuilder()
            .withNewMetadata()
            .withName(randomName)
            .withNamespace(randomNamespace)
            .endMetadata()
            .build()
    );

    var expectedUrl = new URL(stackGresKubernetesClient.getMasterUrl(),
        "/apis/rbac.authorization.k8s.io/v1/namespaces/"
            + randomNamespace + "/roles/" + randomName + getUrlOptions());
    assertEquals(expectedUrl, actual);

    stackGresKubernetesClient.close();
  }

  @Test
  void givenACronJob_ShouldProduceAValidUrl() throws MalformedURLException {

    final StackGresDefaultKubernetesClient stackGresKubernetesClient
        = new StackGresDefaultKubernetesClient();
    var actual = stackGresKubernetesClient.getResourceUrl(new PatchContext.Builder()
            .withFieldManager(randomFieldManager).withForce(true).build(),
        new CronJobBuilder()
            .withNewMetadata()
            .withName(randomName)
            .withNamespace(randomNamespace)
            .endMetadata()
            .build()
    );

    var expectedUrl = new URL(stackGresKubernetesClient.getMasterUrl(),
        "/apis/batch/v1beta1/namespaces/"
            + randomNamespace + "/cronjobs/" + randomName + getUrlOptions());
    assertEquals(expectedUrl, actual);

    stackGresKubernetesClient.close();
  }

  @Test
  void givenAService_shouldProduceAValidUrl() throws MalformedURLException {

    final StackGresDefaultKubernetesClient stackGresKubernetesClient
        = new StackGresDefaultKubernetesClient();

    var actual = stackGresKubernetesClient.getResourceUrl(new PatchContext.Builder()
            .withFieldManager(randomFieldManager).withForce(true).build(),
        new ServiceBuilder()
            .withNewMetadata()
            .withName(randomName)
            .withNamespace(randomNamespace)
            .endMetadata()
            .build()
    );

    var expectedUrl = new URL(stackGresKubernetesClient.getMasterUrl(),
        "/api/v1/namespaces/"
            + randomNamespace + "/services/" + randomName + getUrlOptions());
    assertEquals(expectedUrl, actual);

    stackGresKubernetesClient.close();

  }

  private String getUrlOptions() {
    return "?fieldManager=" + randomFieldManager
        + "&force=true";
  }
}
