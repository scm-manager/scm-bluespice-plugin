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

import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith({MockitoExtension.class, ShiroExtension.class})
public class RepositoryLinkEnricherTest {

  @Mock
  private BlueSpiceContext blueSpiceContext;
  @Mock
  private GlobalBlueSpiceConfig globalConfig;
  @Mock
  private BlueSpiceRepositoryConfig config;

  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;

  private RepositoryLinkEnricher enricher;

  private final static Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  public void init() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    enricher = new RepositoryLinkEnricher(Providers.of(scmPathInfoStore), blueSpiceContext);

    when(context.oneRequireByType(Repository.class)).thenReturn(REPOSITORY);
    when(blueSpiceContext.getConfiguration()).thenReturn(globalConfig);
  }

  @Test
  @SubjectAware(value = "TrainerRed", permissions = "repository:configureBlueSpice:*")
  void shouldAppendConfigLink() {
    when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn(null);
    when(blueSpiceContext.getConfiguration(REPOSITORY)).thenReturn(config);
    when(blueSpiceContext.getConfiguration(REPOSITORY).getRelativePath()).thenReturn(null);
    when(blueSpiceContext.getConfiguration(REPOSITORY).getDirectUrl()).thenReturn(null);
    when(blueSpiceContext.getConfiguration(REPOSITORY).getOverride()).thenReturn(OverrideOption.APPEND);

    enricher.enrich(context, appender);

    verify(appender).appendLink(
            "blueSpiceConfig",
            "https://scm-manager.org/scm/api/v2/bluespice/hitchhiker/HeartOfGold"
    );
  }

  @Test
  @SubjectAware(value = "TrainerRed")
  void shouldNotAppendConfigLinkIfPermissionIsMissing() {
    when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn("https://example.com");
    when(blueSpiceContext.getConfiguration(REPOSITORY)).thenReturn(config);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(eq("blueSpiceConfig"), anyString());
  }

  @Test
  @SubjectAware(value = "TrainerRed", permissions = "repository:configureBlueSpice:*")
  void shouldUseDirectUrl() {
    when(blueSpiceContext.getConfiguration(REPOSITORY)).thenReturn(config);
    when(blueSpiceContext.getConfiguration(REPOSITORY).getRelativePath()).thenReturn(null);
    when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn("https://test.com");
    when(blueSpiceContext.getConfiguration(REPOSITORY).getDirectUrl()).thenReturn("https://example.com");
    when(blueSpiceContext.getConfiguration(REPOSITORY).getOverride()).thenReturn(OverrideOption.OVERRIDE);

    enricher.enrich(context, appender);

    verify(appender).appendLink("blueSpice", "https://example.com");
  }

  @Nested
  class WithBaseUrl {

    @BeforeEach
    public void init() {
      when(blueSpiceContext.getConfiguration(REPOSITORY)).thenReturn(config);
      when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn("https://example.com");
    }

    @Test
    @SubjectAware(value = "TrainerRed", permissions = "repository:configureBlueSpice:*")
    void shouldReturnBaseUrlWhenRelativePathIsNull() {
      when(blueSpiceContext.getConfiguration(REPOSITORY).getRelativePath()).thenReturn(null);
      when(blueSpiceContext.getConfiguration(REPOSITORY).getDirectUrl()).thenReturn(null);
      when(blueSpiceContext.getConfiguration(REPOSITORY).getOverride()).thenReturn(OverrideOption.APPEND);

      enricher.enrich(context, appender);

      verify(appender, times(2)).appendLink(anyString(), anyString());
      verify(appender).appendLink("blueSpice", "https://example.com");
    }

    @Test
    @SubjectAware(value = "TrainerRed", permissions = "repository:configureBlueSpice:*")
    void shouldReturnBaseUrlWhenRelativePathIsEmpty() {
      when(blueSpiceContext.getConfiguration(REPOSITORY).getRelativePath()).thenReturn("");
      when(blueSpiceContext.getConfiguration(REPOSITORY).getDirectUrl()).thenReturn(null);
      when(blueSpiceContext.getConfiguration(REPOSITORY).getOverride()).thenReturn(OverrideOption.APPEND);

      enricher.enrich(context, appender);

      verify(appender, times(2)).appendLink(anyString(), anyString());
      verify(appender).appendLink("blueSpice", "https://example.com/");
    }

    @Test
    @SubjectAware(value = "TrainerRed", permissions = "repository:configureBlueSpice:*")
    void shouldAppendUrlWithPath() {
      when(blueSpiceContext.getConfiguration(REPOSITORY).getRelativePath()).thenReturn("Project_1");
      when(blueSpiceContext.getConfiguration(REPOSITORY).getDirectUrl()).thenReturn(null);
      when(blueSpiceContext.getConfiguration(REPOSITORY).getOverride()).thenReturn(OverrideOption.APPEND);

      enricher.enrich(context, appender);

      verify(appender, times(2)).appendLink(anyString(), anyString());
      verify(appender).appendLink("blueSpice", "https://example.com/Project_1");
    }
  }
}
