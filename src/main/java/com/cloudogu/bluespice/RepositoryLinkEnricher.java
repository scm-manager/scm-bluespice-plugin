/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.bluespice;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final BlueSpiceContext blueSpiceContext;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, BlueSpiceContext blueSpiceContext) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.blueSpiceContext = blueSpiceContext;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), BlueSpiceConfigResource.class);

    if (RepositoryPermissions.custom("configureBlueSpice", repository).isPermitted()) {
      appender.appendLink("blueSpiceConfig", linkBuilder.method("getRepoConfig").parameters(repository.getNamespace(), repository.getName()).href());
    }

    String baseUrl = blueSpiceContext.getConfiguration().getBaseUrl();
    String path = blueSpiceContext.getConfiguration(repository).getRelativePath();
    String directUrl = blueSpiceContext.getConfiguration(repository).getDirectUrl();
    OverrideOption override = blueSpiceContext.getConfiguration(repository).getOverride();
    String link = "blueSpice";

    if (!Strings.isNullOrEmpty(directUrl) && OverrideOption.OVERRIDE.equals(override)) {
      appender.appendLink(link, directUrl);
    } else if (!Strings.isNullOrEmpty(baseUrl) && OverrideOption.APPEND.equals(override)) {
      path = path == null ? "" : "/" + path;
      appender.appendLink(link, baseUrl + path);
    }
  }
}
