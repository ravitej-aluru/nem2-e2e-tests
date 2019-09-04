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

/** Enum containing transaction type constants. */
public enum TransactionType {

  /** RESERVED */
  RESERVED((short) 0),

  // Mosaic
  /** Mosaic definition transaction type. */
  MOSAIC_DEFINITION((short) 0x414D),

  /** Mosaic supply change transaction. */
  MOSAIC_SUPPLY_CHANGE((short) 0x424D),

  // Namespace
  /** Register namespace transaction type. */
  REGISTER_NAMESPACE((short) 0x414E),

  /** Address alias transaction type. */
  ADDRESS_ALIAS((short) 0x424E),

  /** Mosaic alias transaction type. */
  MOSAIC_ALIAS((short) 0x434E),

  // Transfer
  /** Transfer Transaction transaction type. */
  TRANSFER((short) 0x4154),

  // Multisignature
  /** Modify multisig account transaction type. */
  MODIFY_MULTISIG_ACCOUNT((short) 0x4155),

  /** Aggregate complete transaction type. */
  AGGREGATE_COMPLETE((short) 0x4141),

  /** Aggregate bonded transaction type */
  AGGREGATE_BONDED((short) 0x4241),

  /** Hash Lock transaction type */
  LOCK((short) 0x4148),

  // Account filters
  /** Account properties address transaction type */
  ACCOUNT_PROPERTIES_ADDRESS((short) 0x4150),

  /** Account properties mosaic transaction type */
  ACCOUNT_PROPERTIES_MOSAIC((short) 0x4250),

  /** Account properties entity type transaction type */
  ACCOUNT_PROPERTIES_ENTITY_TYPE((short) 0x4350),

  // Metadata
  /** Account metadata */
  ACCOUNT_METADATA((short) 0x4144),

  /** Mosaic metadata */
  MOSAIC_METADATA((short) 0x4244),

  /** Namespace metadata */
  NAMESPACE_METADATA((short) 0x4344),

  // Cross-chain swaps
  /** Secret Lock Transaction type */
  SECRET_LOCK((short) 0x4152),

  /** Secret Proof transaction type */
  SECRET_PROOF((short) 0x4252),

  /** Account link transaction type */
  ACCOUNT_LINK((short) 0x414C);

  private short value;

  TransactionType(final short value) {
    this.value = value;
  }

  /**
   * Static constructor converting transaction type raw value to enum instance.
   *
   * @return {@link TransactionType}
   */
  public static TransactionType rawValueOf(final short value) {
    switch (value) {
      case 16717:
        return TransactionType.MOSAIC_DEFINITION;
      case 16973:
        return TransactionType.MOSAIC_SUPPLY_CHANGE;
      case 16718:
        return TransactionType.REGISTER_NAMESPACE;
      case 16974:
        return TransactionType.ADDRESS_ALIAS;
      case 17230:
        return TransactionType.MOSAIC_ALIAS;
      case 16724:
        return TransactionType.TRANSFER;
      case 16725:
        return TransactionType.MODIFY_MULTISIG_ACCOUNT;
      case 16705:
        return TransactionType.AGGREGATE_COMPLETE;
      case 16961:
        return TransactionType.AGGREGATE_BONDED;
      case 16712:
        return TransactionType.LOCK;
      case 16720:
        return TransactionType.ACCOUNT_PROPERTIES_ADDRESS;
      case 16976:
        return TransactionType.ACCOUNT_PROPERTIES_MOSAIC;
      case 17232:
        return TransactionType.ACCOUNT_PROPERTIES_ENTITY_TYPE;
      case 16722:
        return TransactionType.SECRET_LOCK;
      case 16978:
        return TransactionType.SECRET_PROOF;
      case 16716:
        return TransactionType.ACCOUNT_LINK;
      default:
        throw new IllegalArgumentException(value + " is not a valid value");
    }
  }

  /**
   * Returns enum value.
   *
   * @return enum value
   */
  public short getValue() {
    return this.value;
  }
}
