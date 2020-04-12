package io.nem.symbol.automationHelpers.helper;

// import gherkin.lexer.De;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.sdk.model.transaction.Deadline;
import io.nem.symbol.sdk.model.transaction.Transaction;
import io.nem.symbol.sdk.model.transaction.TransactionFactory;
import io.nem.symbol.sdk.model.transaction.TransactionType;

import java.math.BigInteger;
import java.util.function.Supplier;

public abstract class BaseHelper<U extends BaseHelper> {

  protected final TestContext testContext;
  private Supplier<Deadline> deadlineSupplier;
  private BigInteger maxFee;

  /**
   * Constructor.
   *
   * @param testContext Test context.
   */
  BaseHelper(final TestContext testContext) {
    this.testContext = testContext;
    deadlineSupplier = () -> TransactionHelper.getDefaultDeadline();
    maxFee = BigInteger.ZERO;
  }

  /**
   * Gets the common properties for all transactions.
   *
   * @param factory Transaction factory.
   * @return Transaction.
   */
  protected <T extends Transaction> T buildTransaction(final TransactionFactory<T> factory) {
    final Deadline deadlineLocal = deadlineSupplier.get();
    final T transaction = factory.deadline(deadlineLocal).maxFee(maxFee).build();
    final int fee = maxFee.intValue() != 0 ? maxFee.intValue() :
            // TODO: calculate the number of cosigners for bonded but for now set to max of 25.
            (transaction.getSize() + (transaction.getType() == TransactionType.AGGREGATE_BONDED ? 96 * 25 : 0))
            * testContext.getConfigFileReader().getMinFeeMultiplier().intValue();
    return factory.deadline(deadlineLocal).maxFee(BigInteger.valueOf(fee)).build();
  }

  private U getThis() {
    return (U) this;
  }

  /**
   * Set the deadline.
   *
   * @param deadlineSupplier Deadline supplier.
   * @return this
   */
  public U withDeadline(final Supplier<Deadline> deadlineSupplier) {
    this.deadlineSupplier = deadlineSupplier;
    return getThis();
  }

  /**
   * Set the max fee for the transaction.
   *
   * @param maxFee Max fee.
   * @return this
   */
  public U withMaxFee(final BigInteger maxFee) {
    this.maxFee = maxFee;
    return getThis();
  }
}
