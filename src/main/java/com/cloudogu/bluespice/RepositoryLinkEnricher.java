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

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.api.v2.resources.*;
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
    String baseUrl = blueSpiceContext.getConfiguration().getBaseUrl();
    if (baseUrl != null) {
      if (RepositoryPermissions.custom("configureBlueSpice", repository).isPermitted()) {
      appender.appendLink("blueSpiceConfig", linkBuilder.method("getRepoConfig").parameters(repository.getNamespace(), repository.getName()).href());
      }
      String path = blueSpiceContext.getConfiguration(repository).getPath();
      if (path != null) {
        if (!baseUrl.endsWith("/")) {
          baseUrl = baseUrl + "/";
        }
        appender.appendLink("blueSpice", baseUrl + path);
      } else {
        appender.appendLink("blueSpice", baseUrl);
      }
    }
  }
}
