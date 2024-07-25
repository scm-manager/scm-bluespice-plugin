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

import com.google.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Path("v2/bluespice")
public class BlueSpiceConfigResource {

  private final RepositoryManager repositoryManager;
  private final BlueSpiceConfigStore configStore;
  private final BlueSpiceConfigMapper mapper;

  @Inject
  public BlueSpiceConfigResource(RepositoryManager repositoryManager, BlueSpiceConfigStore configStore, BlueSpiceConfigMapper mapper) {
    this.repositoryManager = repositoryManager;
    this.configStore = configStore;
    this.mapper = mapper;
  }

  @GET
  @Path("{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfiguration(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom("blueSpiceConfiguration", repository).check();
    BlueSpiceConfiguration configuration = configStore.getConfiguration(repository);
    return Response.ok(mapper.map(configuration, repository)).build();
  }

  @PUT
  @Path("/{namespace}/{name}")
  @Consumes({MediaType.APPLICATION_JSON})
  public void updateConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name, BlueSpiceConfigDto updatedConfig) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom("blueSpiceConfiguration", repository).check();
    configStore.storeConfiguration(mapper.map(updatedConfig, configStore.getConfiguration(repository)), repository.getId());
  }

  private Repository loadRepository(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw notFound(entity(new NamespaceAndName(namespace, name)));
    }
    return repository;
  }
}
