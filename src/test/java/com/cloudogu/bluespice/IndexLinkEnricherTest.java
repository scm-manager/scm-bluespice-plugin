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
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IndexLinkEnricherTest {

  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;
  @Mock
  private Subject subject;

  private IndexLinkEnricher enricher;

  @BeforeEach
  public void init() {
    ThreadContext.bind(subject);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    enricher = new IndexLinkEnricher(Providers.of(scmPathInfoStore));
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotAppendConfigLink() {
    when(subject.isPermitted("configuration:read:blueSpice")).thenReturn(false);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  void shouldAppendConfigLink() {
    when(subject.isPermitted("configuration:read:blueSpice")).thenReturn(true);

    enricher.enrich(context, appender);

    verify(appender, atMostOnce()).appendLink(anyString(), anyString());
    verify(appender).appendLink("blueSpiceConfig", "https://scm-manager.org/scm/api/v2/bluespice/");
  }
}
