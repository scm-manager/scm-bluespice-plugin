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

import jakarta.ws.rs.core.MediaType;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.JsonMockHttpRequest;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
public class BlueSpiceConfigResourceTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private BlueSpiceContext context;

  private RestDispatcher dispatcher;

  private final ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
  private final String domain = "http://localhost:8080/scm/api/";
  private final GlobalBlueSpiceConfig globalConfig = new GlobalBlueSpiceConfig();
  private final GlobalBlueSpiceConfigMapper globalConfigMapper = new GlobalBlueSpiceConfigMapperImpl();
  private final BlueSpiceRepositoryConfigMapper repoConfigMapper = new BlueSpiceRepositoryConfigMapperImpl();

  @BeforeEach
  void init() {
    BlueSpiceConfigResource resource = new BlueSpiceConfigResource(repositoryManager, context, globalConfigMapper, repoConfigMapper);

    pathInfoStore.set(() -> URI.create(domain));
    globalConfigMapper.setScmPathInfoStore(pathInfoStore);
    repoConfigMapper.setScmPathInfoStore(pathInfoStore);
    repoConfigMapper.setBlueSpiceContext(context);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
    globalConfig.setBaseUrl("https://example.com");
  }

  @Test
  @SubjectAware(value = "TrainerRed", permissions = {"configuration:read:blueSpice", "configuration:write:blueSpice"})
  void shouldGetGlobalConfig() throws URISyntaxException {
    when(context.getConfiguration()).thenReturn(globalConfig);

    MockHttpRequest request = MockHttpRequest.get("/v2/bluespice/");
    JsonMockHttpResponse response = new JsonMockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);

    GlobalBlueSpiceConfigDto responseBody = response.getContentAs(GlobalBlueSpiceConfigDto.class);
    assertThat(responseBody.getBaseUrl()).isEqualTo(globalConfig.getBaseUrl());
    assertThat(responseBody.getLinks().getLinkBy("self").get().getHref()).isEqualTo(domain + "v2/bluespice/");
    assertThat(responseBody.getLinks().getLinkBy("update").get().getHref()).isEqualTo(domain + "v2/bluespice/");
  }

  @Test
  @SubjectAware(value = "TrainerRed")
  void shouldNotGetGlobalConfigBecausePermissionIsMissing() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/bluespice/");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  @SubjectAware(value = "TrainerRed", permissions = "configuration:write:blueSpice")
  void shouldUpdateGlobalConfig() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest.put("/v2/bluespice/")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .json("{ 'baseUrl': 'https://test.com' }");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    GlobalBlueSpiceConfig expectedConfig = new GlobalBlueSpiceConfig();
    expectedConfig.setBaseUrl("https://test.com");
    verify(context).storeConfiguration(expectedConfig);
  }

  @Test
  @SubjectAware(value = "TrainerRed")
  void shouldNotUpdateGlobalConfigBecauseOfMissingPermission() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest.put("/v2/bluespice/")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .json("{ 'baseUrl': 'https://test.com' }");

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Nested
  class WithRepository {

    private final Repository repository = RepositoryTestData.createHeartOfGold();
    private final String uri = "/v2/bluespice/" + repository.getNamespace() + "/" + repository.getName();

    @BeforeEach
    void init() {
      when(repositoryManager.get(new NamespaceAndName(repository.getNamespace(), repository.getName()))).thenReturn(repository);
    }

    @Test
    @SubjectAware(value = "TrainerRed", permissions = "repository:configureBlueSpice:id-1")
    void shouldGetRepositoryConfig() throws URISyntaxException {
      BlueSpiceRepositoryConfig config = new BlueSpiceRepositoryConfig();
      config.setRelativePath("/project1");
      config.setDirectUrl("https://example.com/project1");
      config.setOverride(OverrideOption.APPEND);
      when(context.getConfiguration()).thenReturn(globalConfig);
      when(context.getConfiguration(repository)).thenReturn(config);

      MockHttpRequest request = MockHttpRequest.get(uri);
      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);

      BlueSpiceRepositoryConfigDto responseBody = response.getContentAs(BlueSpiceRepositoryConfigDto.class);

      assertThat(responseBody.getRelativePath()).isEqualTo(config.getRelativePath());
      assertThat(responseBody.getDirectUrl()).isEqualTo(config.getDirectUrl());
      assertThat(responseBody.getOverride()).isEqualTo(config.getOverride());

      assertThat(responseBody.getLinks().getLinkBy("self").get().getHref())
        .isEqualTo(domain + "v2/bluespice/" + repository.getNamespaceAndName().toString());
      assertThat(responseBody.getLinks().getLinkBy("update").get().getHref())
        .isEqualTo(domain + "v2/bluespice/" + repository.getNamespaceAndName().toString());
      assertThat(responseBody.getLinks().getLinkBy("baseUrl").get().getHref())
        .isEqualTo(globalConfig.getBaseUrl());
    }

    @Test
    @SubjectAware(value = "TrainerRed")
    void shouldNotGetRepositoryConfigBecauseOfMissingPermission() throws URISyntaxException {
      repository.setId("id-1");

      MockHttpRequest request = MockHttpRequest.get(uri);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    @SubjectAware(value = "TrainerRed", permissions = "repository:configureBlueSpice:id-1")
    void shouldUpdateRepositoryConfig() throws URISyntaxException {
      repository.setId("id-1");

      JsonMockHttpRequest request = JsonMockHttpRequest.put(uri)
        .contentType(MediaType.APPLICATION_JSON_TYPE)
        .json("{ 'relativePath': '/project1', 'directUrl': 'https://example.com/project1', 'override': 'OVERRIDE' }");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
      BlueSpiceRepositoryConfig expectedConfig = new BlueSpiceRepositoryConfig();
      expectedConfig.setRelativePath("/project1");
      expectedConfig.setDirectUrl("https://example.com/project1");
      expectedConfig.setOverride(OverrideOption.OVERRIDE);
      verify(context).storeConfiguration(expectedConfig, repository.getId());
    }

    @Test
    @SubjectAware(value = "TrainerRed")
    void shouldNotUpdateRepositoryConfigBecauseOfMissingPermission() throws URISyntaxException {
      repository.setId("id-1");

      JsonMockHttpRequest request = JsonMockHttpRequest.put(uri)
        .contentType(MediaType.APPLICATION_JSON_TYPE)
        .json("{ 'relativePath': '/project1', 'directUrl': 'https://example.com/project1', 'override': 'OVERRIDE' }");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
    }
  }
}

