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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalBlueSpiceConfigMapperTest {
  
  private final URI baseUri = URI.create("https://scm-manager.org/scm/api/");
  private URI expectedBaseUri;

  @Mock
  private Subject subject;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  GlobalBlueSpiceConfigMapperImpl mapper;

  @BeforeEach
  void init() {
    lenient().when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve("v2/bluespice/");
  }

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @Test
  void shouldMapAttributeToDto() {
    GlobalBlueSpiceConfig configuration = new GlobalBlueSpiceConfig();
    configuration.setBaseUrl("https://example.com/");
    GlobalBlueSpiceConfigDto dto = mapper.map(configuration);
    assertThat(dto.getBaseUrl()).isEqualTo("https://example.com/");
  }

  @Test
  void shouldAddHalLinksToDto() {
    when(subject.isPermitted("configuration:write:blueSpice")).thenReturn(true);
    when(subject.isPermitted("configuration:read:blueSpice")).thenReturn(true);

    GlobalBlueSpiceConfigDto dto = mapper.map(new GlobalBlueSpiceConfig());

    assertThat(Objects.requireNonNull(dto.getLinks().getLinkBy("self").orElse(null)).getHref()).isEqualTo(expectedBaseUri.toString());
    assertThat(Objects.requireNonNull(dto.getLinks().getLinkBy("update").orElse(null)).getHref()).isEqualTo(expectedBaseUri.toString());
  }

  @Test
  void shouldNotAddUpdateLinkToDtoIfNotPermitted() {
    GlobalBlueSpiceConfigDto dto = mapper.map(new GlobalBlueSpiceConfig());
    assertThat(dto.getLinks().getLinkBy("update").isPresent()).isFalse();
  }

  @Test
  void shouldMapAttributeFromDto() {
    GlobalBlueSpiceConfig oldConfiguration = new GlobalBlueSpiceConfig();
    GlobalBlueSpiceConfigDto dto = new GlobalBlueSpiceConfigDto();
    GlobalBlueSpiceConfig configuration = mapper.map(dto, oldConfiguration);
    assertThat(configuration.getBaseUrl()).isEqualTo(dto.getBaseUrl());
  }
}
