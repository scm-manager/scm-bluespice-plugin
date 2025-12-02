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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.*;
import sonia.scm.api.v2.resources.HalAppenderMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class BlueSpiceRepositoryConfigMapper extends HalAppenderMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;
  @Inject
  private BlueSpiceContext blueSpiceContext;

  @VisibleForTesting
  void setScmPathInfoStore(ScmPathInfoStore scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @VisibleForTesting
  void setBlueSpiceContext(BlueSpiceContext blueSpiceContext) {
    this.blueSpiceContext = blueSpiceContext;
  }

  public abstract BlueSpiceRepositoryConfigDto map(BlueSpiceRepositoryConfig config, @Context Repository repository);

  public abstract BlueSpiceRepositoryConfig map(BlueSpiceRepositoryConfigDto dto, @Context BlueSpiceRepositoryConfig oldConfig);

  @AfterMapping
  public void appendLinks(@MappingTarget BlueSpiceRepositoryConfigDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(self(repository));
    if (RepositoryPermissions.custom("configureBlueSpice", repository).isPermitted()) {
      linksBuilder.single(Link.link("update", update(repository)));
      String baseUrl = baseUrl();
      if (baseUrl != null && !baseUrl.isEmpty()) {
        linksBuilder.single(Link.link("baseUrl", baseUrl));
      }
      target.add(linksBuilder.build());
    }
  }

  private String self(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), BlueSpiceConfigResource.class);
    return linkBuilder.method("getRepoConfig").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private String update(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), BlueSpiceConfigResource.class);
    return linkBuilder.method("updateRepoConfig").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private String baseUrl() {
    return blueSpiceContext.getConfiguration().getBaseUrl();
  }
}
