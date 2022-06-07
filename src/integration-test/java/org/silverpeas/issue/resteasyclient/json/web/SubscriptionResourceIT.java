package org.silverpeas.issue.resteasyclient.json.web;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.issue.resteasyclient.json.model.Subscription;
import org.silverpeas.issue.resteasyclient.json.test.AbstractSubscriptionIT;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class SubscriptionResourceIT extends AbstractSubscriptionIT {

  @Deployment
  public static Archive<?> createDeployment() {
    return createBaseDeploymentFor(SubscriptionResourceIT.class).addPackages(true,
        SubscriptionResourceIT.class.getPackage());
  }

  @Test
  public void emptyTest() {
    assertThat(true, is(true));
  }

  @Test
  public void existingSubscriptionShouldBeGet() {
    final String subscriptionId = "c91c83f8-8df4-44de-9465-d78f94b4c958";
    final String subscriptionURI = uriOf(subscriptionId);
    SubscriptionEntity entity =
        getAt(subscriptionURI, MediaType.APPLICATION_JSON_TYPE, SubscriptionEntity.class);
    assertThat(entity, notNullValue());

    Subscription subscription = Subscription.getById(subscriptionId).orElseThrow();
    assertThat(entity.getUri().toString().endsWith(WebApplication.RS_BASE_PATH + subscriptionURI),
        is(true));
    assertThat(entity.getFirstName(), is(subscription.getFirstName()));
    assertThat(entity.getLastName(), is(subscription.getLastName()));
    assertThat(entity.getEmail(), is(subscription.getEmail()));
  }

  @Test
  public void newSubscriptionShouldBePosted() {
    SubscriptionEntity subscription = SubscriptionEntity.from(
        new Subscription("Joel", "Meyerowitz", "joel.meyerowitz@joelmeyerowitz.com"));
    Response response =
        post(subscription, MediaType.APPLICATION_JSON_TYPE, SubscriptionResource.BASE_PATH);
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.CREATED));

    // check the returned entity matches the expected
    SubscriptionEntity created = response.readEntity(SubscriptionEntity.class);
    assertThat(created.getFirstName(), is("Joel"));
    assertThat(created.getLastName(), is("Meyerowitz"));
    assertThat(created.getEmail(), is ("joel.meyerowitz@joelmeyerowitz.com"));

    // check now the subscription has been well created by the REST service
    String[] pathParts = created.getUri().getPath().split("/");
    String newSubscriptionId = pathParts[pathParts.length - 1];
    Subscription newSubscription = Subscription.getById(newSubscriptionId).orElseThrow();
    assertThat(created.getFirstName(), is(newSubscription.getFirstName()));
    assertThat(created.getLastName(), is(newSubscription.getLastName()));
    assertThat(created.getEmail(), is(newSubscription.getEmail()));
  }

  private static String uriOf(final String subscriptionId) {
    return "/" + SubscriptionResource.BASE_PATH + "/" + subscriptionId;
  }

  private <C> C getAt(String uri, MediaType mediaType, Class<C> c) {
    Invocation.Builder requestBuilder = prepareInvocation(uri, mediaType.toString());
    return requestBuilder.get(c);
  }

  private <C> Response post(final C entity, MediaType mediaType, String atURI) {
    Invocation.Builder resourcePoster = prepareInvocation(atURI, mediaType.toString());
    return resourcePoster.post(Entity.entity(entity, mediaType.toString()));
  }

  private Invocation.Builder prepareInvocation(final String uri, final String mediaType) {
    String thePath = uri;
    String queryParams = "";
    WebTarget resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      thePath = pathParts[0];
      queryParams = pathParts[1];
    }
    return applyQueryParameters(queryParams, resource.path(thePath)).request(mediaType);
  }

  private WebTarget applyQueryParameters(String parameterQueryPart, WebTarget resource) {
    MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
    String[] queryParameters = parameterQueryPart.split("&");
    for (String aQueryParameter : queryParameters) {
      if (aQueryParameter != null && !aQueryParameter.isBlank()) {
        String[] parameterParts = aQueryParameter.split("=");
        parameters.add(parameterParts[0], parameterParts.length > 1 ? parameterParts[1] : "");
      }
    }
    WebTarget newResource = resource;
    for (Map.Entry<String, List<String>> parameter : parameters.entrySet()) {
      newResource = newResource.queryParam(parameter.getKey(), parameter.getValue().toArray());
    }
    return newResource;
  }

  private WebTarget resource() {
    return ClientBuilder.newClient().target(
        URI.create("http://localhost:8080/") + this.getClass().getSimpleName() + "-test" +
            WebApplication.RS_BASE_PATH);
  }
}
