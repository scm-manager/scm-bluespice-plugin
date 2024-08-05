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

import jakarta.ws.rs.core.MediaType;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BlueSpiceConfigResourceTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private BlueSpiceContext context;
  @Mock
  private GlobalBlueSpiceConfigMapper globalConfigMapper;
  @Mock
  private BlueSpiceRepositoryConfigMapper configMapper;
  @Mock
  private Subject subject;

  private RestDispatcher dispatcher;
  private final GlobalBlueSpiceConfiguration globalConfig = new GlobalBlueSpiceConfiguration();

  @BeforeEach
  void init() {
    ThreadContext.bind(subject);
    BlueSpiceConfigResource resource = new BlueSpiceConfigResource(repositoryManager, context, globalConfigMapper, configMapper);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
    globalConfig.setBaseUrl("https://example.com");
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldGetGlobalConfig() throws URISyntaxException {
    when(context.getConfiguration()).thenReturn(globalConfig);
    when(globalConfigMapper.map(context.getConfiguration())).thenReturn(new GlobalBlueSpiceConfigDto());

    MockHttpRequest request = MockHttpRequest.get("/v2/bluespice/");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void shouldNotGetGlobalConfig() throws URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:read:blueSpice");

    MockHttpRequest request = MockHttpRequest.get("/v2/bluespice/");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  void shouldUpdateGlobalConfig() throws URISyntaxException {
    GlobalBlueSpiceConfigDto configDto = new GlobalBlueSpiceConfigDto();
    GlobalBlueSpiceConfiguration updatedConfig = new GlobalBlueSpiceConfiguration();
    updatedConfig.setBaseUrl("https://test.com");
    when(context.getConfiguration()).thenReturn(globalConfig);
    when(globalConfigMapper.map(configDto, context.getConfiguration())).thenReturn(updatedConfig);

    MockHttpRequest request = MockHttpRequest.put("/v2/bluespice/")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content("{ \"baseUrl\": \"https://test.com\" }".getBytes());
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  void shouldNotUpdateGlobalConfig() throws URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:blueSpice");

    MockHttpRequest request = MockHttpRequest.put("/v2/bluespice/")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content("{ \"baseUrl\": \"https://test.com\" }".getBytes());
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Nested
  class WithRepository {

    Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

    String URI = "/v2/bluespice/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();

    @BeforeEach
    void init() {
      when(repositoryManager.get(new NamespaceAndName(REPOSITORY.getNamespace(), REPOSITORY.getName()))).thenReturn(REPOSITORY);
    }

    @Test
    void shouldGetRepositoryConfig() throws URISyntaxException {
      BlueSpiceRepositoryConfiguration config = new BlueSpiceRepositoryConfiguration();
      config.setPath("Project 1");
      when(context.getConfiguration(REPOSITORY)).thenReturn(config);
      when(configMapper.map(context.getConfiguration(REPOSITORY), REPOSITORY)).thenReturn(new BlueSpiceRepositoryConfigDto());

      MockHttpRequest request = MockHttpRequest.get(URI);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldNotGetRepositoryConfig() throws URISyntaxException {
      REPOSITORY.setId("id-1");
      doThrow(AuthorizationException.class).when(subject).checkPermission("repository:configureBlueSpice:id-1");

      MockHttpRequest request = MockHttpRequest.get(URI);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void shouldUpdateRepositoryConfig() throws URISyntaxException {
      BlueSpiceRepositoryConfiguration config = new BlueSpiceRepositoryConfiguration();
      config.setPath("Project 1");
      BlueSpiceRepositoryConfigDto configDto = new BlueSpiceRepositoryConfigDto();
      BlueSpiceRepositoryConfiguration updatedConfig = new BlueSpiceRepositoryConfiguration();
      updatedConfig.setPath("Project 2");
      when(context.getConfiguration(REPOSITORY)).thenReturn(config);
      when(configMapper.map(configDto, context.getConfiguration(REPOSITORY))).thenReturn(updatedConfig);

      MockHttpRequest request = MockHttpRequest.put(URI)
        .contentType(MediaType.APPLICATION_JSON_TYPE)
        .content("{ \"path\": \"Project 2\" }".getBytes());
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    void shouldNotUpdateRepositoryConfig() throws URISyntaxException {
      REPOSITORY.setId("id-1");
      doThrow(AuthorizationException.class).when(subject).checkPermission("repository:configureBlueSpice:id-1");

      MockHttpRequest request = MockHttpRequest.put(URI)
        .contentType(MediaType.APPLICATION_JSON_TYPE)
        .content("{ \"path\": \"Project 2\" }".getBytes());
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
    }
  }
}

