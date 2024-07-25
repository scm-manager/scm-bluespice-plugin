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
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

public class BlueSpiceConfigStore {

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public BlueSpiceConfigStore(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public BlueSpiceConfiguration getConfiguration(Repository repository) {
    return createStore(repository.getId()).getOptional().orElse(new BlueSpiceConfiguration());
  }

  private ConfigurationStore<BlueSpiceConfiguration> createStore(String repositoryId) {
    return storeFactory.withType(BlueSpiceConfiguration.class).withName("blueSpiceConfig").forRepository(repositoryId).build();
  }

  public void storeConfiguration(BlueSpiceConfiguration configuration, String repositoryId) {
    createStore(repositoryId).set(configuration);
  }
}
