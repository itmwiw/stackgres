/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.cluster;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.stackgres.operator.customresource.sgbackupconfig.StackGresBackupConfigSpec;
import io.stackgres.operator.customresource.storages.BackupStorage;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

public abstract class AbstractBackupSecret {

  protected ImmutableMap<String, String> getBackupSecrets(
      StackGresBackupConfigSpec backupConfigSpec, Map<String, Map<String, String>> secrets) {
    return Seq.of(
        Optional.of(backupConfigSpec)
        .map(StackGresBackupConfigSpec::getPgpConfiguration)
        .map(pgpConf -> Seq.of(
            getSecretEntry("WALG_PGP_KEY", pgpConf.getKey(), secrets))),
        Optional.of(backupConfigSpec)
        .map(StackGresBackupConfigSpec::getStorage)
        .map(BackupStorage::getS3)
        .map(awsConf -> Seq.of(
            getSecretEntry("AWS_ACCESS_KEY_ID",
                awsConf.getCredentials().getAccessKey(), secrets),
            getSecretEntry("AWS_SECRET_KEY_ID",
                awsConf.getCredentials().getSecretKey(), secrets))),
        Optional.of(backupConfigSpec)
        .map(StackGresBackupConfigSpec::getStorage)
        .map(BackupStorage::getGcs)
        .map(gcsConfig -> Seq.of(
            getSecretEntry(
                getGcsCredentialsFileName(),
                gcsConfig.getCredentials().getServiceAccountJsonKey(),
                secrets))),
        Optional.of(backupConfigSpec)
        .map(StackGresBackupConfigSpec::getStorage)
        .map(BackupStorage::getAzureblob)
        .map(azureConfig -> Seq.of(
            getSecretEntry("AZURE_STORAGE_ACCOUNT",
                azureConfig.getCredentials().getAccount(), secrets),
            getSecretEntry("AZURE_STORAGE_ACCESS_KEY",
                azureConfig.getCredentials().getAccessKey(), secrets))))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(s -> s)
        .collect(ImmutableMap.toImmutableMap(t -> t.v1, t -> t.v2));
  }

  protected String getGcsCredentialsFileName() {
    return ClusterStatefulSet.GCS_CREDENTIALS_FILE_NAME;
  }

  private Tuple2<String, String> getSecretEntry(String envvar,
      SecretKeySelector secretKeySelector, Map<String, Map<String, String>> secrets) {
    return Tuple.tuple(envvar, secrets.get(secretKeySelector.getName())
        .get(secretKeySelector.getKey()));
  }
}
