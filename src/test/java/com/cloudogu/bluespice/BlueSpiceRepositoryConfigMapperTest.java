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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlueSpiceRepositoryConfigMapperTest {

  private final static Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private final URI baseUri = URI.create("https://scm-manager.org/scm/api/");
  private URI expectedBaseUri;

  @Mock
  private BlueSpiceContext blueSpiceContext;

  @Mock
  private GlobalBlueSpiceConfig globalConfig;

  @Mock
  private Subject subject;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  BlueSpiceRepositoryConfigMapperImpl mapper;

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
    BlueSpiceRepositoryConfig configuration = new BlueSpiceRepositoryConfig();
    configuration.setRelativePath("Project1");
    BlueSpiceRepositoryConfigDto dto = mapper.map(configuration, REPOSITORY);
    assertThat(dto.getRelativePath()).isEqualTo("Project1");
  }

  @Test
  void shouldAddHalLinksToDto() {
    when(subject.isPermitted("repository:configureBlueSpice:" + REPOSITORY.getId())).thenReturn(true);
    when(blueSpiceContext.getConfiguration()).thenReturn(globalConfig);
    when(blueSpiceContext.getConfiguration().getBaseUrl()).thenReturn("https://example.com");
    BlueSpiceRepositoryConfigDto dto = mapper.map(new BlueSpiceRepositoryConfig(), REPOSITORY);

    String expectedUrl = expectedBaseUri.toString() + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();
    assertThat(Objects.requireNonNull(dto.getLinks().getLinkBy("self").orElse(null)).getHref()).isEqualTo(expectedUrl);
    assertThat(Objects.requireNonNull(dto.getLinks().getLinkBy("update").orElse(null)).getHref()).isEqualTo(expectedUrl);
    assertThat(Objects.requireNonNull(dto.getLinks().getLinkBy("baseUrl").orElse(null)).getHref()).isEqualTo("https://example.com");
  }

  @Test
  void shouldNotAddUpdateLinkToDtoIfNotPermitted() {
    BlueSpiceRepositoryConfigDto dto = mapper.map(new BlueSpiceRepositoryConfig(), REPOSITORY);
    assertThat(dto.getLinks().getLinkBy("update").isPresent()).isFalse();
  }

  @Test
  void shouldMapAttributeFromDto() {
    BlueSpiceRepositoryConfig oldConfiguration = new BlueSpiceRepositoryConfig();
    BlueSpiceRepositoryConfigDto dto = new BlueSpiceRepositoryConfigDto();
    BlueSpiceRepositoryConfig configuration = mapper.map(dto, oldConfiguration);
    assertThat(configuration.getRelativePath()).isEqualTo(dto.getRelativePath());
  }
}
