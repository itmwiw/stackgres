/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.dbops;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.stackgres.common.ErrorType;
import io.stackgres.common.StackGresComponent;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterDbOpsMajorVersionUpgradeStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterDbOpsStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterStatus;
import io.stackgres.common.crd.sgdbops.StackGresDbOps;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfig;
import io.stackgres.common.resource.CustomResourceFinder;
import io.stackgres.operator.common.StackGresDbOpsReview;
import io.stackgres.operator.validation.ValidationType;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;
import org.jooq.lambda.tuple.Tuple2;

@Singleton
@ValidationType(ErrorType.FORBIDDEN_CR_UPDATE)
public class DbOpsMajorVersionUpgradeValidator implements DbOpsValidator {

  private final CustomResourceFinder<StackGresCluster> clusterFinder;
  private final CustomResourceFinder<StackGresPostgresConfig> postgresConfigFinder;
  private final List<String> supportedPostgresVersions;
  private final String errorCrReferencerUri;
  private final String errorPostgresMismatchUri;
  private final String errorForbiddenUpdateUri;

  @Inject
  public DbOpsMajorVersionUpgradeValidator(
      CustomResourceFinder<StackGresCluster> clusterFinder,
      CustomResourceFinder<StackGresPostgresConfig> postgresConfigFinder) {
    this(clusterFinder, postgresConfigFinder,
        StackGresComponent.POSTGRESQL.getOrderedVersions().toList());
  }

  public DbOpsMajorVersionUpgradeValidator(
      CustomResourceFinder<StackGresCluster> clusterFinder,
      CustomResourceFinder<StackGresPostgresConfig> postgresConfigFinder,
      List<String> orderedSupportedPostgresVersions) {
    this.clusterFinder = clusterFinder;
    this.postgresConfigFinder = postgresConfigFinder;
    this.supportedPostgresVersions = new ArrayList<String>(orderedSupportedPostgresVersions);
    this.errorCrReferencerUri = ErrorType.getErrorTypeUri(ErrorType.INVALID_CR_REFERENCE);
    this.errorPostgresMismatchUri = ErrorType.getErrorTypeUri(ErrorType.PG_VERSION_MISMATCH);
    this.errorForbiddenUpdateUri = ErrorType.getErrorTypeUri(ErrorType.FORBIDDEN_CR_UPDATE);
  }

  @Override
  public void validate(StackGresDbOpsReview review) throws ValidationFailed {
    switch (review.getRequest().getOperation()) {
      case CREATE:
        StackGresDbOps dbOps = review.getRequest().getObject();
        if (dbOps.getSpec().isOpMajorVersionUpgrade()) {
          Optional<StackGresPostgresConfig> postgresConfig = postgresConfigFinder
              .findByNameAndNamespace(
                  dbOps.getSpec().getMajorVersionUpgrade().getSgPostgresConfig(),
                  dbOps.getMetadata().getNamespace());

          Optional<StackGresCluster> cluster = clusterFinder.findByNameAndNamespace(
              dbOps.getSpec().getSgCluster(), dbOps.getMetadata().getNamespace());
          if (cluster.isPresent()) {
            String givenPgVersion = dbOps.getSpec().getMajorVersionUpgrade().getPostgresVersion();

            if (givenPgVersion != null && !isPostgresVersionSupported(givenPgVersion)) {
              final String message = "Unsupported postgres version " + givenPgVersion
                  + ".  Supported postgres versions are: "
                  + StackGresComponent.POSTGRESQL.getOrderedVersions().toString(", ");
              fail(errorPostgresMismatchUri, message);
            }

            String givenMajorVersion = StackGresComponent.POSTGRESQL.findMajorVersion(
                givenPgVersion);
            long givenMajorVersionIndex = StackGresComponent.POSTGRESQL.getOrderedMajorVersions()
                .zipWithIndex()
                .filter(t -> t.v1.equals(givenMajorVersion))
                .map(Tuple2::v2)
                .findAny()
                .get();
            String oldPgVersion = Optional.ofNullable(cluster.get().getStatus())
                .map(StackGresClusterStatus::getDbOps)
                .map(StackGresClusterDbOpsStatus::getMajorVersionUpgrade)
                .map(StackGresClusterDbOpsMajorVersionUpgradeStatus::getSourcePostgresVersion)
                .orElse(cluster.get().getSpec().getPostgres().getVersion());
            String oldMajorVersion = StackGresComponent.POSTGRESQL.findMajorVersion(oldPgVersion);
            long oldMajorVersionIndex = StackGresComponent.POSTGRESQL.getOrderedMajorVersions()
                .zipWithIndex()
                .filter(t -> t.v1.equals(oldMajorVersion))
                .map(Tuple2::v2)
                .findAny()
                .get();

            if (givenMajorVersionIndex >= oldMajorVersionIndex) {
              fail(errorForbiddenUpdateUri,
                  "postgres version must be a newer major version than the current one");
            }

            if (postgresConfig.isPresent()) {
              if (!postgresConfig.get().getSpec().getPostgresVersion().equals(givenMajorVersion)) {
                fail(errorCrReferencerUri, "SGPostgresConfig must be for postgres version "
                    + givenMajorVersion);
              }
            } else {
              fail(errorCrReferencerUri, "SGPostgresConfig "
                  + dbOps.getSpec().getMajorVersionUpgrade().getSgPostgresConfig() + " not found");
            }
          }
        }
        break;
      default:
    }

  }

  private boolean isPostgresVersionSupported(String version) {
    return supportedPostgresVersions.contains(version);
  }

}
