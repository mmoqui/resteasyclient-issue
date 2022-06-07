package org.silverpeas.issue.resteasyclient.json.util;

import javax.enterprise.context.Dependent;
import javax.transaction.Transactional;

@Dependent
public class Transaction {

  public static Transaction getInstance() {
    BeanContainer container = BeanContainer.getInstance();
    return container.getBeanByType(Transaction.class);
  }

  public static <V> V performInOne(final Process<V> process) {
    return getInstance().perform(process);
  }

  public static <V> V performInNew(final Process<V> process) {
    return getInstance().performNew(process);
  }

  @Transactional
  public <V> V perform(final Process<V> process) {
    return process.execute();
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public <V> V performNew(final Process<V> process) {
    return process.execute();
  }

  @FunctionalInterface
  public interface Process<V> {
    V execute();
  }
}
