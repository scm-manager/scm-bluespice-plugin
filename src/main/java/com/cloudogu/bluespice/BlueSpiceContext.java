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

  public GlobalBlueSpiceConfiguration getConfiguration() {
    return createGlobalStore().getOptional().orElse(new GlobalBlueSpiceConfiguration());
  }

  public BlueSpiceRepositoryConfiguration getConfiguration(Repository repository) {
    return createStore(repository.getId()).getOptional().orElse(new BlueSpiceRepositoryConfiguration());
  }

  private ConfigurationStore<BlueSpiceRepositoryConfiguration> createStore(String repositoryId) {
    return storeFactory.withType(BlueSpiceRepositoryConfiguration.class).withName(NAME).forRepository(repositoryId).build();
  }

  private ConfigurationStore<GlobalBlueSpiceConfiguration> createGlobalStore() {
    return storeFactory.withType(GlobalBlueSpiceConfiguration.class).withName(NAME).build();
  }

  public void storeConfiguration(GlobalBlueSpiceConfiguration configuration) {
    createGlobalStore().set(configuration);
  }

  public void storeConfiguration(BlueSpiceRepositoryConfiguration configuration, String repositoryId) {
    createStore(repositoryId).set(configuration);
  }
}
