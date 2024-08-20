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
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static com.cloudogu.bluespice.BlueSpiceContext.NAME;

@OpenAPIDefinition(tags = {
  @Tag(name = "BlueSpice Plugin", description = "BlueSpice plugin provided endpoints")
})
@Path("v2/bluespice")
public class BlueSpiceConfigResource {

  private final RepositoryManager repositoryManager;
  private final BlueSpiceContext context;
  private final GlobalBlueSpiceConfigMapper globalConfigMapper;
  private final BlueSpiceRepositoryConfigMapper configMapper;

  @Inject
  public BlueSpiceConfigResource(RepositoryManager repositoryManager, BlueSpiceContext context, GlobalBlueSpiceConfigMapper globalConfigMapper, BlueSpiceRepositoryConfigMapper configMapper) {
    this.repositoryManager = repositoryManager;
    this.context = context;
    this.globalConfigMapper = globalConfigMapper;
    this.configMapper = configMapper;
  }

  @GET
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(
    summary = "Get global BlueSpice configuration",
    description = "Returns the global BlueSpice configuration.",
    tags = "BlueSpice Plugin",
    operationId = "bluespice_get_global_config")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = GlobalBlueSpiceConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the configuration")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getGlobalConfig() {
    ConfigurationPermissions.read(NAME).check();
    return Response.ok(globalConfigMapper.map(context.getConfiguration())).build();
  }

  @PUT
  @Path("/")
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(
    summary = "Update global BlueSpice configuration",
    description = "Modifies the global BlueSpice configuration.",
    tags = "BlueSpice Plugin",
    operationId = "bluespice_put_global_config")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege to change the configuration")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response updateGlobalConfig(GlobalBlueSpiceConfigDto updatedConfig) {
    ConfigurationPermissions.write(NAME).check();
    context.storeConfiguration(globalConfigMapper.map(updatedConfig, context.getConfiguration()));
    return Response.noContent().build();
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Get repository BlueSpice configuration",
    description = "Returns the repository BlueSpice configuration.",
    tags = "BlueSpice Plugin",
    operationId = "bluespice_get_repo_config"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = BlueSpiceRepositoryConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the configuration")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getRepoConfig(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom("configureBlueSpice", repository).check();
    return Response.ok(configMapper.map(context.getConfiguration(repository), repository)).build();
  }

  @PUT
  @Path("/{namespace}/{name}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(
    summary = "Modify repository BlueSpice configuration",
    description = "Modifies the repository BlueSpice configuration.",
    tags = "BlueSpice Plugin",
    operationId = "bluespice_put_repo_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege to change the configuration")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response updateRepoConfig(@PathParam("namespace") String namespace, @PathParam("name") String name, BlueSpiceRepositoryConfigDto updatedConfig) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom("configureBlueSpice", repository).check();
    context.storeConfiguration(configMapper.map(updatedConfig, context.getConfiguration(repository)), repository.getId());
    return Response.noContent().build();
  }

  private Repository loadRepository(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw notFound(entity(new NamespaceAndName(namespace, name)));
    }
    return repository;
  }
}
