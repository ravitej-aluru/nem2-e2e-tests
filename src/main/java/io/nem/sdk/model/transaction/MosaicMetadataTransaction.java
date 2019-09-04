/*
 * Copyright 2018 NEM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nem.sdk.model.transaction;

import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import org.apache.commons.lang3.Validate;

import java.math.BigInteger;
import java.util.Optional;

/** Namespace metadata transaction. */
public final class MosaicMetadataTransaction extends Transaction {
  /** Metadata target public key. */
  private final PublicAccount ownerPublicAccount;
  /** Metadata key scoped to source, target and type. */
  private final BigInteger scopedMetadataKey;
  /** Target mosaic identifier. */
  private final io.nem.sdk.model.transaction.UInt64Id targetMosaicId;
  /** Change in value size in bytes. */
  private final short valueSizeDelta;
  /**
   * Difference between existing value and new value \note when there is no existing value, new
   * value is same this value \note when there is an existing value, new value is calculated as
   * xor(previous-value, value).
   */
  private final byte[] value;

  /**
   * Constructor.
   *
   * @param networkType Network type;
   * @param version Version.
   * @param maxFee Max transaction fee.
   * @param deadline Transaction deadline.
   * @param ownerPublicAccount Namespace owner public account.
   * @param scopedMetadataKey Metadata key scoped to source, target and type.
   * @param targetMosaicId Target namespace identifier.
   * @param valueSizeDelta Change in value size in bytes.
   * @param value Difference between existing value and new value \note when there is no existing
   *     value, new value is same this value \note when there is an existing value, new value is
   *     calculated as xor(previous-value, value).
   * @param signature Entity signature.
   * @param signer Entity signer's public key.
   * @param transactionInfo Transaction info.
   */
  protected MosaicMetadataTransaction(
      final NetworkType networkType,
      final short version,
      final Deadline deadline,
      final BigInteger maxFee,
      final PublicAccount ownerPublicAccount,
      final BigInteger scopedMetadataKey,
      final UInt64Id targetMosaicId,
      final short valueSizeDelta,
      final byte[] value,
      final Optional<String> signature,
      final Optional<PublicAccount> signer,
      final Optional<TransactionInfo> transactionInfo) {
    super(
        TransactionType.MOSAIC_METADATA,
        networkType,
        version,
        deadline,
        maxFee,
        signature,
        signer,
        transactionInfo);
    Validate.notNull(ownerPublicAccount, "targetPublicKey is null");
    Validate.notNull(targetMosaicId, "targetNamespaceId is null");
    Validate.notNull(value, "value is null");
    this.ownerPublicAccount = ownerPublicAccount;
    this.scopedMetadataKey = scopedMetadataKey;
    this.targetMosaicId = targetMosaicId;
    this.valueSizeDelta = valueSizeDelta;
    this.value = value;
  }

  /**
   * Constructor.
   *
   * @param deadline Transaction deadline.
   * @param maxFee Transaction fee.
   * @param ownerPublicAccount Namespace owner public key.
   * @param scopedMetadataKey Metadata key scoped to source, target and type.
   * @param targetMosaicId Target namespace identifier.
   * @param valueSizeDelta Change in value size in bytes.
   * @param value Difference between existing value and new value \note when there is no existing
   *     value, new value is same this value \note when there is an existing value, new value is
   *     calculated as xor(previous-value, value).
   */
  protected MosaicMetadataTransaction(
      final Deadline deadline,
      final BigInteger maxFee,
      final PublicAccount ownerPublicAccount,
      final BigInteger scopedMetadataKey,
      final UInt64Id targetMosaicId,
      final short valueSizeDelta,
      final byte[] value,
      final NetworkType networkType) {
    this(
        networkType,
        TransactionVersion.MOSAIC_METADATA.getValue(),
        deadline,
        maxFee,
        ownerPublicAccount,
        scopedMetadataKey,
        targetMosaicId,
        valueSizeDelta,
        value,
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
  }

  /**
   * Creates an instance of NamespaceMetadataTransactionBuilder.
   *
   * @param deadline Transaction deadline.
   * @param maxFee Transaction fee.
   * @param targetPublicKey Metadata target public key.
   * @param scopedMetadataKey Metadata key scoped to source, target and type.
   * @param targetMosaicId Target unresolved mosaic identifier.
   * @param valueSizeDelta Change in value size in bytes.
   * @param value Difference between existing value and new value \note when there is no existing
   *     value, new value is same this value \note when there is an existing value, new value is
   *     calculated as xor(previous-value, value).
   * @return Instance of NamespaceMetadataTransactionBuilder.
   */
  public static MosaicMetadataTransaction create(
      final Deadline deadline,
      final BigInteger maxFee,
      final PublicAccount targetPublicKey,
      final BigInteger scopedMetadataKey,
      final UInt64Id targetMosaicId,
      final short valueSizeDelta,
      final byte[] value,
      final NetworkType networkType) {
    return new MosaicMetadataTransaction(
        deadline,
        maxFee,
        targetPublicKey,
        scopedMetadataKey,
        targetMosaicId,
        valueSizeDelta,
        value,
        networkType);
  }

  /**
   * Creates an instance of NamespaceMetadataTransactionBuilder.
   *
   * @param deadline Transaction deadline.
   * @param targetPublicKey Metadata target public key.
   * @param scopedMetadataKey Metadata key scoped to source, target and type.
   * @param targetMosaicId Target unresolved mosaic identifier.
   * @param valueSizeDelta Change in value size in bytes.
   * @param value Difference between existing value and new value \note when there is no existing
   *     value, new value is same this value \note when there is an existing value, new value is
   *     calculated as xor(previous-value, value).
   * @return Instance of NamespaceMetadataTransactionBuilder.
   */
  public static MosaicMetadataTransaction create(
      final Deadline deadline,
      final PublicAccount targetPublicKey,
      final BigInteger scopedMetadataKey,
      final UInt64Id targetMosaicId,
      final short valueSizeDelta,
      final byte[] value,
      final NetworkType networkType) {
    final BigInteger maxFee = BigInteger.ZERO;
    return new MosaicMetadataTransaction(
        deadline,
        maxFee,
        targetPublicKey,
        scopedMetadataKey,
        targetMosaicId,
        valueSizeDelta,
        value,
        networkType);
  }

  /**
   * Gets metadata target public key.
   *
   * @return Metadata target public key.
   */
  public PublicAccount getOwnerPublicAccount() {
    return ownerPublicAccount;
  }

  /**
   * Gets metadata key scoped to source, target and type.
   *
   * @return Metadata key scoped to source, target and type.
   */
  public BigInteger getScopedMetadataKey() {
    return scopedMetadataKey;
  }

  /**
   * Gets target mosaic identifier.
   *
   * @return Target mosaic identifier.
   */
  public UInt64Id getTargetMosaicId() {
    return targetMosaicId;
  }

  /**
   * Gets change in value size in bytes.
   *
   * @return Change in value size in bytes.
   */
  public short getValueSizeDelta() {
    return valueSizeDelta;
  }

  /**
   * Gets difference between existing value and new value \note when there is no existing value, new
   * value is same this value \note when there is an existing value, new value is calculated as
   * xor(previous-value, value).
   *
   * @return Difference between existing value and new value \note when there is no existing value,
   *     new value is same this value \note when there is an existing value, new value is calculated
   *     as xor(previous-value, value).
   */
  public byte[] getValue() {
    return value;
  }

  /**
   * Gets the size of the object.
   *
   * @return Size in bytes.
   */
  public int getSize() {
    int size = 0;
    return size;
  }

  /** @return */
  @Override
  byte[] generateBytes() {
    return new byte[0];
  }

  /**
   * Geneterate the
   *
   * @return
   */
  @Override
  byte[] generateEmbeddedBytes() {
    return new byte[0];
  }
}
