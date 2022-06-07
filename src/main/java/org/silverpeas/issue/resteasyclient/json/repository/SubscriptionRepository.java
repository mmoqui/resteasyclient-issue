package org.silverpeas.issue.resteasyclient.json.repository;

import org.silverpeas.issue.resteasyclient.json.model.Subscription;
import org.silverpeas.issue.resteasyclient.json.util.BeanContainer;

import java.util.Optional;

public interface SubscriptionRepository {

  static SubscriptionRepository getInstance() {
    BeanContainer container = BeanContainer.getInstance();
    return container.getBeanByType(SubscriptionRepository.class);
  }

  Subscription put(final Subscription subscription);

  void remove(final Subscription subscription);

  Optional<Subscription> get(final String id);
}
