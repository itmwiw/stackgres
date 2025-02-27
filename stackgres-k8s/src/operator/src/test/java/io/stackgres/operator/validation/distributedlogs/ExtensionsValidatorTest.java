/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.distributedlogs;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.stackgres.common.ErrorType;
import io.stackgres.common.OperatorProperty;
import io.stackgres.common.StackGresComponent;
import io.stackgres.common.crd.sgcluster.StackGresClusterInstalledExtension;
import io.stackgres.operator.common.StackGresDistributedLogsReview;
import io.stackgres.operator.utils.ValidationUtils;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;
import io.stackgres.testutil.JsonUtil;
import org.jooq.lambda.Seq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtensionsValidatorTest {

  private static final String POSTGRES_MAJOR_VERSION =
      StackGresComponent.POSTGRESQL.getOrderedMajorVersions().findFirst().get();

  private static final String BUILD_VERSION =
      StackGresComponent.POSTGRESQL.getOrderedBuildVersions().findFirst().get();

  private ExtensionsValidator validator;

  private List<StackGresClusterInstalledExtension> defaultExtensions;

  @BeforeEach
  void setUp() {
    validator = new ExtensionsValidator();

    defaultExtensions = Seq.of(
        "plpgsql",
        "pg_stat_statements",
        "dblink",
        "plpython3u")
        .map(this::getDefaultExtension)
        .collect(ImmutableList.toImmutableList());
  }

  @Test
  void givenAValidCreation_shouldPass() throws ValidationFailed {
    final StackGresDistributedLogsReview review = getCreationReview();
    review.getRequest().getObject().getSpec().setToInstallPostgresExtensions(new ArrayList<>());
    review.getRequest().getObject().getSpec().getToInstallPostgresExtensions()
        .addAll(defaultExtensions);
    validator.validate(review);
  }

  @Test
  void givenAnUpdate_shouldPass() throws ValidationFailed {
    final StackGresDistributedLogsReview review = getUpdateReview();
    review.getRequest().getObject().getSpec().setToInstallPostgresExtensions(new ArrayList<>());
    review.getRequest().getObject().getSpec().getToInstallPostgresExtensions()
        .addAll(defaultExtensions);
    validator.validate(review);
  }

  @Test
  void givenACreationWithMissingExtensions_shouldFail() {
    final StackGresDistributedLogsReview review = getCreationReview();

    ValidationUtils.assertValidationFailed(() -> validator.validate(review),
        ErrorType.MISSING_EXTENSION,
        "Extensions dblink, pg_stat_statements, plpgsql, plpython3u are missing.");
  }

  private StackGresDistributedLogsReview getCreationReview() {
    return JsonUtil
        .readFromJson("distributedlogs_allow_request/create.json",
            StackGresDistributedLogsReview.class);
  }

  private StackGresDistributedLogsReview getUpdateReview() {
    return JsonUtil
        .readFromJson("distributedlogs_allow_request/update.json",
            StackGresDistributedLogsReview.class);
  }

  private StackGresClusterInstalledExtension getDefaultExtension(String name) {
    final StackGresClusterInstalledExtension installedExtension =
        new StackGresClusterInstalledExtension();
    installedExtension.setName(name);
    installedExtension.setPublisher("com.ongres");
    installedExtension.setRepository(OperatorProperty.EXTENSIONS_REPOSITORY_URLS.getString());
    installedExtension.setVersion("1.0.0");
    installedExtension.setPostgresVersion(POSTGRES_MAJOR_VERSION);
    installedExtension.setBuild(BUILD_VERSION);
    return installedExtension;
  }

}
