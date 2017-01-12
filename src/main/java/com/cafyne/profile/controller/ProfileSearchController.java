package com.cafyne.profile.controllers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cafyne.profile.model.ProfileSearchDTO;
import com.cafyne.profile.service.impl.ProfileHandler;
import com.cafyne.web.controller.BaseController;
import com.cafyne.web.hateoas.HateoasResource;
import com.cafyne.web.hateoas.HateoasSupport;
import com.cafyne.web.resources.AuthDTO;
import com.cafyne.web.resources.CafyneResourceActions;
import com.cafyne.web.resources.CafyneResources;
import com.cafyne.web.resources.HasPermission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/profiles-search")
@Produces("application/json")
@Consumes("application/json")
@Component
@Api("Profile Search Apis")
public class ProfileSearchController implements HateoasSupport, BaseController {

	private static Logger LOG = LoggerFactory.getLogger(ProfileSearchController.class);
	private @Autowired ProfileHandler profileHandler;

	@ApiOperation(value = "Creates a new profile discovery search", response = ProfileSearchDTO.class)
	@POST
	public Response add(ProfileSearchDTO profileSearchDto, @Context UriInfo uriInfo,
			@HasPermission(name = CafyneResources.profile, actions = { CafyneResourceActions.CREATE,
					CafyneResourceActions.READ }) @HeaderParam("authorization") String auth) {
		LOG.info("Saving profile for id: {} ", profileSearchDto);
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.discoverProfiles(populateUserDetails(profileSearchDto, authDTO)))
				.build();
	}

	@ApiOperation(value = "Deletes a profile discovery search", response = String.class)
	@POST
	@Path("delete")
	public Response delete(ProfileSearchDTO profileSearchDto, @Context UriInfo uriInfo,
			@HasPermission(name = CafyneResources.profile, actions = CafyneResourceActions.DELETE) @HeaderParam("authorization") String auth) {
		LOG.info("Deleting search history for query: {} ", profileSearchDto.getQuery());
		profileHandler.deleteSearch(profileSearchDto);
		String response = "{\"desc\":\"Profile search deleted successfully\"}";
		return Response.ok().entity(response).build();
	}

	@ApiOperation(value = "Loads all the profile discovery results", response = ProfileSearchDTO.class)
	@GET
	public Response get( @Context UriInfo uriInfo,
			@HasPermission(name = CafyneResources.profile, actions = CafyneResourceActions.READ) @HeaderParam("authorization") String auth) {
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.getSearched(authDTO.get_org(),
				authDTO.get_groups().get(0).getId(), authDTO.getUsername())).build();
	}
	
	@ApiOperation(value = "Loads the monitoring status of discovered profiles")
	@GET
	@Path("/discovery-results/{key}")
	public Response get(@PathParam("key") String dbId, @Context UriInfo uriInfo,
			@HasPermission(name = CafyneResources.profile, actions = CafyneResourceActions.READ) @HeaderParam("authorization") String auth) {
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.getDiscoveryResultById(dbId, authDTO.get_org(),
				authDTO.get_groups().get(0).getId(), authDTO.getUsername())).build();
	}
	

	@ApiOperation(value = "Test endpoint to see if profile-search micro service is up and running")
	@GET
	@Path("/test")
	public Response test(@Context UriInfo uriInfo) {
		return Response.ok().entity("profiles-search microservice working").build();
	}

	@Override
	public void updateResponseForHateoas(HateoasResource hateoasResource, UriInfo uriInfo) {
		// TODO Auto-generated method stub

	}

	private static ProfileSearchDTO populateUserDetails(ProfileSearchDTO profileDTO, AuthDTO authDTO) {
		profileDTO.setCompanyId(authDTO.get_org());
		profileDTO.setGroupId(authDTO.get_groups().get(0).getId());
		profileDTO.setUserId(authDTO.getUsername());
		return profileDTO;
	}
}
