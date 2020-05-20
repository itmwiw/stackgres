/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import io.stackgres.common.ErrorType;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterDefinition;
import io.stackgres.common.crd.sgcluster.StackGresClusterList;
import io.stackgres.common.resource.CustomResourceScanner;
import io.stackgres.testutil.JsonUtil;
import io.stackgres.operator.utils.ValidationUtils;
import io.stackgres.operatorframework.admissionwebhook.AdmissionReview;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public abstract class DependenciesValidatorTest<T extends AdmissionReview<?>, V extends DependenciesValidator<T>> {

  protected DependenciesValidator<T> validator;

  @Mock
  protected CustomResourceScanner<StackGresCluster> clusterScanner;

  protected abstract DependenciesValidator<T> setUpValidation();

  @BeforeEach
  void setUp() {
    validator = setUpValidation();
    validator.setClusterScanner(clusterScanner);
  }

  @Test
  protected abstract void givenAReviewCreation_itShouldDoNothing() throws ValidationFailed;

  protected void givenAReviewCreation_itShouldDoNothing(T review) throws ValidationFailed {

    validator.validate(review);

    verify(clusterScanner, never()).findResources();
    verify(clusterScanner, never()).findResources(anyString());

  }

  @Test
  protected abstract void givenAReviewUpdate_itShouldDoNothing() throws ValidationFailed;

  protected void givenAReviewUpdate_itShouldDoNothing(T review) throws ValidationFailed {
    validator.validate(review);

    verify(clusterScanner, never()).findResources();
    verify(clusterScanner, never()).findResources(anyString());
  }

  @Test
  protected abstract void givenAReviewDelete_itShouldFailIfIsAClusterDependsOnIt();

  protected void givenAReviewDelete_itShouldFailIfIsAClusterDependsOnIt(T review) {

    StackGresClusterList clusterList = JsonUtil.readFromJson("stackgres_cluster/list.json",
        StackGresClusterList.class);

    when(clusterScanner.findResources(review.getRequest().getNamespace()))
        .thenReturn(Optional.of(clusterList.getItems()));

    ValidationFailed ex = ValidationUtils.assertErrorType(ErrorType.FORBIDDEN_CR_DELETION,
        () -> validator.validate(review));

    assertEquals("Can't delete "
            + review.getRequest().getResource().getResource()
            + "." + review.getRequest().getKind().getGroup()
            + " " + review.getRequest().getName() + " because the "
            + StackGresClusterDefinition.NAME + " "
            + clusterList.getItems().get(0).getMetadata().getName() + " depends on it"
        , ex.getResult().getMessage());

  }

  @Test
  protected abstract void givenAReviewDelete_itShouldNotFailIfNotClusterDependsOnIt() throws ValidationFailed;

  protected void givenAReviewDelete_itShouldNotFailIfNotClusterDependsOnIt(T review) throws ValidationFailed {

    when(clusterScanner.findResources(review.getRequest().getNamespace()))
        .thenReturn(Optional.empty());

    validator.validate(review);

    verify(clusterScanner, never()).findResources();
    verify(clusterScanner).findResources(review.getRequest().getNamespace());

  }
}
