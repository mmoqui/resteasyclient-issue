package org.silverpeas.issue.resteasyclient.json.repository.impl;

import org.silverpeas.issue.resteasyclient.json.model.Subscription;
import org.silverpeas.issue.resteasyclient.json.repository.SubscriptionRepository;
import org.silverpeas.issue.resteasyclient.json.util.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Repository
public class DefaultSubscriptionRepository implements SubscriptionRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public Subscription put(final Subscription subscription) {
    Subscription persisted;
    if (subscription.getId() == null) {
      entityManager.persist(subscription);
      persisted = subscription;
    } else {
      persisted = entityManager.merge(subscription);
    }
    return persisted;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void remove(final Subscription subscription) {
    entityManager.remove(entityManager.merge(subscription));
  }

  @Override
  public Optional<Subscription> get(final String id) {
    return ofNullable(entityManager.find(Subscription.class, id));
  }
}
