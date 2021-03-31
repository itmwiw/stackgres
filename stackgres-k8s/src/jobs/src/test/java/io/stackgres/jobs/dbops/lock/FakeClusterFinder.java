/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.dbops.lock;

import java.util.Optional;

import javax.inject.Inject;

import io.quarkus.test.Mock;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.resource.CustomResourceFinder;
import org.jetbrains.annotations.NotNull;

@Mock
public class FakeClusterFinder implements CustomResourceFinder<StackGresCluster> {

  private final MockKubeDb kubeDb;

  @Inject
  public FakeClusterFinder(MockKubeDb kubeDb) {
    this.kubeDb = kubeDb;
  }

  @Override
  public @NotNull Optional<StackGresCluster> findByNameAndNamespace(String name, String namespace) {
    return Optional.ofNullable(kubeDb.getCluster(name, namespace));
  }
}
