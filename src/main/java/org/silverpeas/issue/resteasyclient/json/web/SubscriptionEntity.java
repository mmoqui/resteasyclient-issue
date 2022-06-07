package org.silverpeas.issue.resteasyclient.json.web;

import org.silverpeas.issue.resteasyclient.json.model.Subscription;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SubscriptionEntity {

  @XmlElement
  private URI uri;

  @XmlElement
  private String firstName;

  @XmlElement
  private String lastName;

  @XmlElement
  private String email;

  public static SubscriptionEntity from(final Subscription subscription) {
    SubscriptionEntity entity = new SubscriptionEntity();
    entity.email = subscription.getEmail();
    entity.firstName = subscription.getFirstName();
    entity.lastName = subscription.getLastName();
    return entity;
  }

  private SubscriptionEntity() {
  }

  public SubscriptionEntity withURI(final URI subscriptionUri) {
    this.uri = subscriptionUri;
    return this;
  }

  public URI getUri() {
    return uri;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public Subscription toSubscription() {
    Subscription subscription = new Subscription(firstName, lastName, email);
    if (uri != null) {
      String[] uriParts = uri.toString().split("/");
      String id = uriParts[uriParts.length - 1];
      subscription.setId(id);
    }
    return subscription;
  }
}
