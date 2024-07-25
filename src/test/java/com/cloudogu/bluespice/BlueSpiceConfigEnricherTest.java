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
import org.junit.jupiter.api.BeforeEach;
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
public class BlueSpiceConfigEnricherTest {

  @Mock
  private BlueSpiceConfigStore configStore;
  @Mock
  private BlueSpiceConfiguration configuration;

  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;

  private BlueSpiceConfigEnricher enricher;
  private Repository repository;

  @BeforeEach
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    enricher = new BlueSpiceConfigEnricher(Providers.of(scmPathInfoStore), configStore);

    repository = RepositoryTestData.createHeartOfGold("git");
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(configStore.getConfiguration(repository)).thenReturn(configuration);
  }

  @Test
  void shouldOnlyAppendConfigLink() {
    when(configStore.getConfiguration(repository).getUrl()).thenReturn(null);

    enricher.enrich(context, appender);

    verify(appender, atMostOnce()).appendLink(anyString(), anyString());
    verify(appender).appendLink("blueSpiceConfig", String.format("https://scm-manager.org/scm/api/v2/bluespice/%s/%s", repository.getNamespace(), repository.getName()));
  }

  @Test
  void shouldAppendBlueSpiceLink() {
    when(configStore.getConfiguration(repository).getUrl()).thenReturn("https://example.com/");

    enricher.enrich(context, appender);

    verify(appender, times(2)).appendLink(anyString(), anyString());
    verify(appender).appendLink("blueSpice", "https://example.com/");
  }
}
