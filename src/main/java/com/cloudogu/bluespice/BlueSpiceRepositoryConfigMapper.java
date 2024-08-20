/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
      linksBuilder.single(Link.link("baseUrl", baseUrl()));
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
