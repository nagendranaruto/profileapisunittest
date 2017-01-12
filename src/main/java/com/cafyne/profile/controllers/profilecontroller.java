package com.cafyne.profile.controllers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cafyne.profile.model.ProfileDTO;
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

@Path("/profiles")
@Produces("application/json")
@Consumes("application/json")
@Component
@Api("Profile Apis")
public class ProfileController implements HateoasSupport, BaseController {

	private static Logger LOG = LoggerFactory.getLogger(ProfileController.class);
	private @Autowired ProfileHandler profileHandler;

	@ApiOperation(value = "Creates a new Profile", response = ProfileDTO.class)
	@POST
	public Response add(ProfileDTO profileDto, @Context UriInfo uriInfo,
			@HasPermission(name = CafyneResources.profile, actions = CafyneResourceActions.CREATE) @HeaderParam("authorization") String auth) {
		LOG.info("Saving profile for id: {} ", profileDto.getScreenName());
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.addProfile(populateUserDetails(profileDto, authDTO))).build();
	}

	@ApiOperation(value = "Deletes a profile", response = String.class)
	@POST
	@Path("delete")
	public Response delete(ProfileDTO profileDto, @Context UriInfo uriInfo,
			@HasPermission(name = CafyneResources.profile, actions = {
					CafyneResourceActions.DELETE }) @HeaderParam("authorization") String auth) {
		LOG.info("Deleting profile for id: {} ", profileDto.getScreenName());
		AuthDTO authDTO = buildAuthDTO(auth);
		profileHandler.deleteProfile(populateUserDetails(profileDto, authDTO));
		String response = "{\"desc\":\"Profile deleted successfully\"}";
		return Response.ok().entity(response).build();
	}

	@ApiOperation(value = "Loads all the profiles that were added", response = ProfileDTO.class, responseContainer = "List")
	@GET
	public Response get(@Context UriInfo uriInfo, @HasPermission(name = CafyneResources.profile, actions = {
			CafyneResourceActions.READ }) @HeaderParam("authorization") String auth) {
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.getMonitoredProfiles(authDTO.get_org(), authDTO.get_groups().get(0).getId(),
				authDTO.getUsername())).build();
	}
	
	@ApiOperation(value = "Loads all the profiles that were added", response = ProfileDTO.class, responseContainer = "List")
	@GET
	@Path("/added")
	public Response getAddedProfiles(@Context UriInfo uriInfo, @HasPermission(name = CafyneResources.profile, actions = {
			CafyneResourceActions.READ }) @HeaderParam("authorization") String auth) {
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.getAddedProfiles(authDTO.get_org(), authDTO.get_groups().get(0).getId(),
				authDTO.getUsername())).build();
	}
	
	@ApiOperation(value = "Loads all the profiles that were added", response = ProfileDTO.class, responseContainer = "List")
	@GET
	@Path("/discovered")
	public Response getDiscoveredProfiles(@Context UriInfo uriInfo, @HasPermission(name = CafyneResources.profile, actions = {
			CafyneResourceActions.READ }) @HeaderParam("authorization") String auth) {
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.getDiscoveredProfiles(authDTO.get_org(), authDTO.get_groups().get(0).getId(),
				authDTO.getUsername())).build();
	}

	@ApiOperation(value = "Test endpoint to see if profile micro service is up and running")
	@GET
	@Path("/test")
	public Response test(@Context UriInfo uriInfo) {
		return Response.ok().entity("profile microservice working").build();
	}

	@Override
	public void updateResponseForHateoas(HateoasResource hateoasResource, UriInfo uriInfo) {
		// TODO Auto-generated method stub

	}

	@ApiOperation(value = "Gets all the profiles that were authorized by a user", response = ProfileDTO.class, responseContainer = "List")
	@GET
	@Path("/authorized")
	public Response getAuthorizedProfiles(@PathParam("userId") String userId,
			@HasPermission(name = CafyneResources.profile, actions = {
					CafyneResourceActions.READ }) @HeaderParam("authorization") String auth) {
		LOG.info("get all authorized profiles ");
		AuthDTO authDTO = buildAuthDTO(auth);
		return Response.ok().entity(profileHandler.getAuthorizedProfiles(authDTO.getUsername())).build();
	}

	private static ProfileDTO populateUserDetails(ProfileDTO profileDTO, AuthDTO authDTO) {
		profileDTO.setCompanyId(authDTO.get_org());
		profileDTO.setGroupId(authDTO.get_groups().get(0).getId());
		profileDTO.setUserId(authDTO.getUsername());
		return profileDTO;
	}

}
