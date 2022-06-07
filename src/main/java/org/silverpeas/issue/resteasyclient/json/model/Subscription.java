package org.silverpeas.issue.resteasyclient.json.model;

import org.silverpeas.issue.resteasyclient.json.repository.SubscriptionRepository;
import org.silverpeas.issue.resteasyclient.json.util.Transaction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "sc_subscription")
public class Subscription {

  @NotNull
  @Column(nullable = false)
  private String firstName;

  @NotNull
  @Column(nullable = false)
  private String lastName;

  @NotNull
  @Column(nullable = false)
  private String email;

  @Id
  @Column(nullable = false, unique = true)
  private String id;

  public static Optional<Subscription> getById(final String id) {
    SubscriptionRepository repository = SubscriptionRepository.getInstance();
    return repository.get(id);
  }

  protected Subscription() {
  }

  public Subscription(final String firstName, final String lastName, final String email) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
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

  @SuppressWarnings("unused")
  protected void setFirstName(String name) {
    this.firstName = name;
  }

  public void setLastName(String name) {
    this.lastName = name;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public void setId(final String id) {
    this.id = id;
  }

  @Id
  public String getId() {
    return id;
  }

  public Subscription save() {
    return Transaction.performInOne(() -> SubscriptionRepository.getInstance().put(this));
  }

  public void delete() {
    Transaction.performInOne(() -> {
      SubscriptionRepository.getInstance().remove(this);
      return null;
    });
  }

  @PrePersist
  private void onPersist() {
    if (this.id != null) {
      throw new IllegalStateException("The id is already set!");
    }
    this.id = UUID.randomUUID().toString();
  }
}


