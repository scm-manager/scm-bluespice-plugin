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
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.HalAppenderMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;

import static de.otto.edison.hal.Links.linkingTo;
import static com.cloudogu.bluespice.BlueSpiceContext.NAME;

@Mapper
public abstract class GlobalBlueSpiceConfigMapper extends HalAppenderMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract GlobalBlueSpiceConfigDto map(GlobalBlueSpiceConfig config);

  public abstract GlobalBlueSpiceConfig map(GlobalBlueSpiceConfigDto dto, @Context GlobalBlueSpiceConfig oldConfig);

  @VisibleForTesting
  void setScmPathInfoStore(ScmPathInfoStore scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @AfterMapping
  public void appendLinks(@MappingTarget GlobalBlueSpiceConfigDto target) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write(NAME).isPermitted()) {
      linksBuilder.single(Link.link("update", update()));
    }
    if (ConfigurationPermissions.read(NAME).isPermitted()) {
      target.add(linksBuilder.build());
    }
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), BlueSpiceConfigResource.class);
    return linkBuilder.method("getGlobalConfig").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), BlueSpiceConfigResource.class);
    return linkBuilder.method("updateGlobalConfig").parameters().href();
  }
}
