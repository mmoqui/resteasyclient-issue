package org.silverpeas.issue.resteasyclient.json.model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.issue.resteasyclient.json.test.AbstractSubscriptionIT;
import org.silverpeas.issue.resteasyclient.json.test.DataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class SubscriptionPersistenceIT extends AbstractSubscriptionIT {

  @Deployment
  public static Archive<?> createDeployment() {
    return createBaseDeploymentFor(SubscriptionPersistenceIT.class);
  }

  @Test
  public void emptyTest() {
    assertThat(true, is(true));
  }

  @Test
  public void existingSubscriptionShouldBeFound() {
    final String subscriptionId = "c91c83f8-8df4-44de-9465-d78f94b4c958";
    Optional<Subscription> mayBeASubscription = Subscription.getById(subscriptionId);
    assertThat(mayBeASubscription.isPresent(), is(true));

    Subscription subscription = mayBeASubscription.get();
    assertThat(subscription.getId(), is(subscriptionId));
    assertThat(subscription.getFirstName(), is("John"));
    assertThat(subscription.getLastName(), is("Doo"));
    assertThat(subscription.getEmail(), is("john.doo@silverpeas.org"));
  }

  @Test
  public void nonExistingSubscriptionShouldReturnNothing() {
    Optional<Subscription> mayBeASubscription =
        Subscription.getById("12");
    assertThat(mayBeASubscription.isEmpty(), is(true));
  }

  @Test
  public void existingSubscriptionShouldBeDeleted() {
    final String subscriptionId = "c91c83f8-8df4-44de-9465-d78f94b4c958";
    Subscription subscription = Subscription.getById(subscriptionId).orElseThrow();
    subscription.delete();

    Subscription deleted = selectSubscriptionFromDatabase(subscriptionId);
    assertThat(deleted, nullValue());
  }

  @Test
  public void saveANewSubscriptionShouldPersistIt() {
    Subscription subscription = new Subscription("Bart", "Simpson", "bart.simpson@simpsons.com");
    Subscription saved = subscription.save();
    assertThat(saved.getId(), notNullValue());

    Subscription actual = selectSubscriptionFromDatabase(saved.getId());
    assertThat(actual, notNullValue());
    assertThat(actual.getFirstName(), is(subscription.getFirstName()));
    assertThat(actual.getLastName(), is(subscription.getLastName()));
    assertThat(actual.getEmail(), is(subscription.getEmail()));
  }

  @Test
  public void updateASubscriptionShouldPersistTheChange() {
    final String subscriptionId = "d4df8988-34be-4da2-8503-ac60493dc554";
    Subscription subscription = Subscription.getById(subscriptionId).orElseThrow();

    String email = "toto.tartempion@chez-les-papoos.fr";
    assertThat(subscription.getEmail(), not(email));
    subscription.setEmail("toto.tartempion@chez-les-papoos.fr");
    subscription.save();

    Subscription updated = selectSubscriptionFromDatabase(subscriptionId);
    assertThat(updated, notNullValue());
    assertThat(updated.getEmail(), is(email));
  }

  private Subscription selectSubscriptionFromDatabase(String id) {
    try (Connection connection = DataSourceProvider.getDataSource().getConnection()) {
      try (PreparedStatement statement = connection.prepareStatement(
          "select firstName, lastName, email from sc_subscription where id = ?")) {
        statement.setString(1, id);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            String firstName = rs.getString("firstName");
            String lastName = rs.getString("lastName");
            String email = rs.getString("email");
            Subscription subscription = new Subscription(firstName, lastName, email);
            subscription.setId(id);
            return subscription;
          } else {
            return null;
          }
        }
      }
    } catch (SQLException e) {
      fail(e.getMessage());
      return null;
    }
  }
}
