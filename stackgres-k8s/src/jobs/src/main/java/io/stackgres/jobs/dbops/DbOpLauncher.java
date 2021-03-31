/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.dbops;

public interface DbOpLauncher {

  void launchDbOp(String dbOpName, String namespace);
}
