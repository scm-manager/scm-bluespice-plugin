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

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

@Singleton
public class BlueSpiceContext {

  public static final String NAME = "blueSpice";

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public BlueSpiceContext(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public GlobalBlueSpiceConfig getConfiguration() {
    return createGlobalStore().getOptional().orElse(new GlobalBlueSpiceConfig());
  }

  public BlueSpiceRepositoryConfig getConfiguration(Repository repository) {
    return createStore(repository.getId()).getOptional().orElse(new BlueSpiceRepositoryConfig());
  }

  private ConfigurationStore<BlueSpiceRepositoryConfig> createStore(String repositoryId) {
    return storeFactory.withType(BlueSpiceRepositoryConfig.class).withName(NAME).forRepository(repositoryId).build();
  }

  private ConfigurationStore<GlobalBlueSpiceConfig> createGlobalStore() {
    return storeFactory.withType(GlobalBlueSpiceConfig.class).withName(NAME).build();
  }

  public void storeConfiguration(GlobalBlueSpiceConfig configuration) {
    String baseUrl = configuration.getBaseUrl();
    if(baseUrl.endsWith("/")) {
      configuration.setBaseUrl(baseUrl.substring(0, baseUrl.length() - 1));
    }
    createGlobalStore().set(configuration);
  }

  public void storeConfiguration(BlueSpiceRepositoryConfig configuration, String repositoryId) {
    GlobalBlueSpiceConfig globalConfig = getConfiguration();
    if(Strings.isNullOrEmpty(globalConfig.getBaseUrl())) {
      configuration.setOverride(OverrideOption.OVERRIDE);
    }
    String relativePath = configuration.getRelativePath();
    if(!Strings.isNullOrEmpty(relativePath) && relativePath.startsWith("/")) {
      configuration.setRelativePath(relativePath.replaceFirst("/",""));
    }
    createStore(repositoryId).set(configuration);
  }
}
