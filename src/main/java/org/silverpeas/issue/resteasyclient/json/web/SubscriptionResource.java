package org.silverpeas.issue.resteasyclient.json.web;

import org.silverpeas.issue.resteasyclient.json.model.Subscription;
import org.silverpeas.issue.resteasyclient.json.util.WebResource;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.silverpeas.issue.resteasyclient.json.web.SubscriptionResource.BASE_PATH;

@WebResource
@Path(BASE_PATH)
public class SubscriptionResource {

  public static final String BASE_PATH = "subscriptions";

  @Context
  private UriInfo uriInfo;

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public SubscriptionEntity getSubscriptionById(@PathParam("id") String id) {
    Subscription subscription = findSubscription(id);
    return SubscriptionEntity.from(subscription).withURI(uriInfo.getRequestUri());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response createSubscription(final SubscriptionEntity entity) {
    Subscription subscription = entity.toSubscription();
    subscription = subscription.save();
    URI uri = uriInfo.getRequestUriBuilder().path(String.valueOf(subscription.getId())).build();
    SubscriptionEntity saved = SubscriptionEntity.from(subscription).withURI(uri);
    return Response.created(uri).entity(saved).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}")
  @Transactional
  public SubscriptionEntity updateSubscription(@PathParam("id") String id,
      final SubscriptionEntity entity) {
    Subscription subscription = findSubscription(id);
    Subscription newState = entity.toSubscription();
    if (!subscription.getId().equals(newState.getId())) {
      throw new BadRequestException(
          "The new state doesn't refer to the referred subscription");
    }

    subscription.setLastName(newState.getLastName());
    subscription.setEmail(newState.getEmail());
    Subscription updated = subscription.save();
    return SubscriptionEntity.from(updated).withURI(uriInfo.getRequestUri());
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public void deleteSubscription(@PathParam("id") String id) {
    Subscription subscription = findSubscription(id);
    subscription.delete();
  }

  private Subscription findSubscription(final String id) {
    return Subscription.getById(id)
        .orElseThrow(() -> new NotFoundException("No such subscription " + id));
  }
}
