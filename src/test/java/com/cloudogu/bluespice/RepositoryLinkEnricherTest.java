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

import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RepositoryLinkEnricherTest {

  @Mock
  private BlueSpiceContext blueSpiceContext;
  @Mock
  private GlobalBlueSpiceConfiguration globalConfig;
  @Mock
  private BlueSpiceRepositoryConfiguration config;

  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;
  @Mock
  private Subject subject;

  private RepositoryLinkEnricher enricher;

  private final static Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  public void init() {
    ThreadContext.bind(subject);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    enricher = new RepositoryLinkEnricher(Providers.of(scmPathInfoStore), blueSpiceContext);

    when(context.oneRequireByType(Repository.class)).thenReturn(REPOSITORY);
    when(blueSpiceContext.getConfiguration()).thenReturn(globalConfig);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotAppendConfigLinkIfBaseUrlMissing() {
    when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn(null);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  void shouldNotAppendConfigLinkIfPermissionMissing() {
    when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn("https://example.com");
    when(blueSpiceContext.getConfiguration(REPOSITORY)).thenReturn(config);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(eq("blueSpiceConfig"), anyString());
  }

  @Nested
  class WithBaseUrl {

    @BeforeEach
    public void init() {
      when(blueSpiceContext.getConfiguration(REPOSITORY)).thenReturn(config);
      when(subject.isPermitted("repository:configureBlueSpice:" + REPOSITORY.getId())).thenReturn(true);
      when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn("https://example.com");
    }

    @Test
    void shouldAppendUrl() {
      when(blueSpiceContext.getConfiguration(REPOSITORY).getPath()).thenReturn(null);

      enricher.enrich(context, appender);

      verify(appender, times(2)).appendLink(anyString(), anyString());
      verify(appender).appendLink("blueSpice", "https://example.com");
    }

    @Test
    void shouldAppendUrlWithPath() {
      when(blueSpiceContext.getConfiguration(REPOSITORY).getPath()).thenReturn("Project_1");

      enricher.enrich(context, appender);

      verify(appender, times(2)).appendLink(anyString(), anyString());
      verify(appender).appendLink("blueSpice", "https://example.com/Project_1");
    }

    @Test
    void shouldAppendUrlWithEndingSlashAndPath() {
      when(blueSpiceContext.getConfiguration(REPOSITORY).getPath()).thenReturn("Project_1");
      when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn("https://example.com/");

      enricher.enrich(context, appender);

      verify(appender, times(2)).appendLink(anyString(), anyString());
      verify(appender).appendLink("blueSpice", "https://example.com/Project_1");
    }
  }
}
