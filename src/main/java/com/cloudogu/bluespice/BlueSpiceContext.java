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
