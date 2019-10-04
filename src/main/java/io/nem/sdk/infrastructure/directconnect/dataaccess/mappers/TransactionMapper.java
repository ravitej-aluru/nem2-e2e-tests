/**
 * ** Copyright (c) 2016-present,
 * ** Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
 * **
 * ** This file is part of Catapult.
 * **
 * ** Catapult is free software: you can redistribute it and/or modify
 * ** it under the terms of the GNU Lesser General Public License as published by
 * ** the Free Software Foundation, either version 3 of the License, or
 * ** (at your option) any later version.
 * **
 * ** Catapult is distributed in the hope that it will be useful,
 * ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 * ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * ** GNU Lesser General Public License for more details.
 * **
 * ** You should have received a copy of the GNU Lesser General Public License
 * ** along with Catapult. If not, see <http://www.gnu.org/licenses/>.
 **/

package io.nem.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.core.utils.HexEncoder;
import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.mosaic.*;
import io.nem.sdk.model.namespace.AliasAction;
import io.nem.sdk.model.namespace.NamespaceId;
import io.nem.sdk.model.namespace.NamespaceType;
import io.nem.sdk.model.transaction.*;
import io.vertx.core.json.JsonObject;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Transaction mapper. */
public class TransactionMapper implements Function<JsonObject, Transaction> {
  /* Transaction json. */
  JsonObject transaction;
  /* Transaction deadline. */
  Deadline deadline;
  /* Network type. */
  NetworkType networkType;
  /* Transaction version. */
  Short version;
  /* Max fee. */
  BigInteger maxFee;
  /* Transaction info. */
  TransactionInfo transactionInfo;
  /* Transaction type. */
  TransactionType type = TransactionType.RESERVED;
  /* Signer public account */
  PublicAccount signer;
  /* Transaction signature */
  String signature;

  /**
   * Gets the common properties for all transactions.
   *
   * @param jsonObject Json object.
   */
  protected void extractCommonProperties(final JsonObject jsonObject) {
    if (type == TransactionType.RESERVED) {
      transactionInfo = this.createTransactionInfo(jsonObject.getJsonObject("meta"));
      transaction = jsonObject.getJsonObject("transaction");
      type = TransactionType.rawValueOf(transaction.getInteger("type").shortValue());
      deadline = new Deadline(extractBigInteger(transaction, "deadline"));
      networkType = extractNetworkType(transaction.getInteger("version"));
      version = extractTransactionVersion(transaction.getInteger("version")).shortValue();
      maxFee = extractBigInteger(transaction, "maxFee");
      signature = transaction.getString("signature");
      signer = new PublicAccount(transaction.getString("signerPublicKey"), networkType);
    }
  }

  /**
   * Converts from Json to a transaction.
   *
   * @param jsonObject Json object.
   * @return Transaction.
   */
  @Override
  public Transaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    if (type == TransactionType.TRANSFER) {
      return new TransferTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.NAMESPACE_REGISTRATION) {
      return new RegisterNamespaceTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.MOSAIC_DEFINITION) {
      return new MosaicDefinitionTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.MOSAIC_SUPPLY_CHANGE) {
      return new MosaicSupplyChangeTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.MODIFY_MULTISIG_ACCOUNT) {
      return new MultisigModificationTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.AGGREGATE_COMPLETE
        || type == TransactionType.AGGREGATE_BONDED) {
      return new AggregateTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.LOCK) {
      return new LockFundsTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.SECRET_LOCK) {
      return new SecretLockTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.SECRET_PROOF) {
      return new SecretProofTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.MOSAIC_ALIAS) {
      return new MosaicAliasTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.ADDRESS_ALIAS) {
      return new AddressAliasTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.ACCOUNT_PROPERTIES_MOSAIC) {
      return new AccountMosaicRestrictionModificationTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.ACCOUNT_PROPERTIES_ADDRESS) {
      return new AccountAddressRestrictionModificationTransactionMapper().apply(jsonObject);
    } else if (type == TransactionType.ACCOUNT_PROPERTIES_ENTITY_TYPE) {
      return new AccountOperationRestrictionModificationTransactionMapper().apply(jsonObject);
    }
    throw new UnsupportedOperationException("Unimplemented Transaction type");
  }

  /**
   * Gets a BigInteger from the json.
   *
   * @param jsonObject Json object.
   * @param name Property name.
   * @return BigInteger value of name.
   */
  protected BigInteger extractBigInteger(final JsonObject jsonObject, final String name) {
    return BigInteger.valueOf(jsonObject.getLong(name));
  }

  /**
   * Gets the transaction version.
   *
   * @param version Transaction network version.
   * @return Transaction version.
   */
  private Integer extractTransactionVersion(final int version) {
    return (int) Long.parseLong(Integer.toHexString(version).substring(2, 4), 16);
  }

  /**
   * Gets the network type.
   *
   * @param version Transaction network version.
   * @return Network type.
   */
  private NetworkType extractNetworkType(final int version) {
    int networkType = (int) Long.parseLong(Integer.toHexString(version).substring(0, 2), 16);
    return NetworkType.rawValueOf(networkType);
  }

  /**
   * Creates the transaction info.
   *
   * @param jsonObject Json object.
   * @return Transaction info.
   */
  private TransactionInfo createTransactionInfo(final JsonObject jsonObject) {
    if (jsonObject.containsKey("hash")) {
      return TransactionInfo.create(
          extractBigInteger(jsonObject, "height"),
          jsonObject.getInteger("index"),
          "",
          jsonObject.getString("hash"),
          jsonObject.getString("merkleComponentHash"));
    } else if (jsonObject.containsKey("aggregateHash")) {
      return TransactionInfo.createAggregate(
          extractBigInteger(jsonObject, "height"),
          jsonObject.getInteger("index"),
          "",
          jsonObject.getString("aggregateHash"),
          jsonObject.getJsonObject("aggregateId").getString("$oid"));
    } else {
      return TransactionInfo.create(
          extractBigInteger(jsonObject, "height"), jsonObject.getString("hash"));
    }
  }
}

/** Transfer transaction mapper. */
class TransferTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to transfer transaction.
   *
   * @param jsonObject Json object.
   * @return Transfer transaction.
   */
  @Override
  public TransferTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    List<Mosaic> mosaics = new ArrayList<>();
    if (transaction.getJsonArray("mosaics") != null) {
      mosaics =
          transaction.getJsonArray("mosaics").stream()
              .map(item -> (JsonObject) item)
              .map(new MosaicMapper())
              .collect(Collectors.toList());
    }

    Message message = PlainMessage.Empty;
    if (transaction.getJsonObject("message") != null) {
      message =
          new PlainMessage(
              new String(
                  Hex.decode(transaction.getJsonObject("message").getString("payload")),
                  StandardCharsets.UTF_8));
    }
    final String recipient = transaction.getString("recipientAddress");
    if (recipient.startsWith("01")) {
      final String namespaceString = recipient.substring(1, 10);
      final byte[] bytes = HexEncoder.getBytes(namespaceString);
      final BigInteger bigInteger = new BigInteger(bytes);
      return new TransferTransaction(
          networkType,
          version,
          deadline,
          maxFee,
          new NamespaceId(bigInteger),
          mosaics,
          message,
          signature,
          signer,
          transactionInfo);
    }
    return new TransferTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        Address.createFromEncoded(recipient),
        mosaics,
        message,
        signature,
        signer,
        transactionInfo);
  }
}

/** Register namespace transaction mapper. */
class RegisterNamespaceTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to register namespace transaction.
   *
   * @param jsonObject Json object.
   * @return Register namespace transaction.
   */
  @Override
  public NamespaceRegistrationTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    final NamespaceType namespaceType =
        NamespaceType.rawValueOf(transaction.getInteger("registrationType"));

    return new NamespaceRegistrationTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        transaction.getString("name"),
        new NamespaceId(extractBigInteger(transaction, "id")),
        namespaceType,
        namespaceType == NamespaceType.RootNamespace
            ? Optional.of(extractBigInteger(transaction, "duration"))
            : Optional.empty(),
        namespaceType == NamespaceType.SubNamespace
            ? Optional.of(new NamespaceId(extractBigInteger(transaction, "parentId")))
            : Optional.empty(),
        signature,
        signer,
        transactionInfo);
  }
}

/** Mosaic definition transaction mapper. */
class MosaicDefinitionTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to mosaic definition transaction.
   *
   * @param jsonObject Json object.
   * @return Mosaic definition transaction.
   */
  @Override
  public MosaicDefinitionTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    final MosaicProperties properties = new MosaicPropertiesMapper().apply(transaction);

    return new MosaicDefinitionTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        MosaicNonce.createFromBigInteger(extractBigInteger(transaction, "nonce")),
        new MosaicId(extractBigInteger(transaction, "id")),
        properties,
        signature,
        signer,
        transactionInfo);
  }
}

/** Mosaic supply change transaction mapper. */
class MosaicSupplyChangeTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to mosaic supply change transaction.
   *
   * @param jsonObject Json object.
   * @return Mosaic supply change transaction.
   */
  @Override
  public MosaicSupplyChangeTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    return new MosaicSupplyChangeTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        new MosaicId(extractBigInteger(transaction, "mosaicId")),
        MosaicSupplyType.rawValueOf(transaction.getInteger("action")),
        extractBigInteger(transaction, "delta"),
        signature,
        signer,
        transactionInfo);
  }
}

/** Multisig modification transaction mapper. */
class MultisigModificationTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to multisig modification transaction.
   *
   * @param jsonObject Json object.
   * @return Multisig modification transaction.
   */
  @Override
  public ModifyMultisigAccountTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    final List<MultisigCosignatoryModification> modifications =
        transaction.containsKey("modifications")
            ? transaction.getJsonArray("modifications").stream()
                .map(item -> (JsonObject) item)
                .map(
                    multisigModification ->
                        new MultisigCosignatoryModification(
                            MultisigCosignatoryModificationType.rawValueOf(
                                multisigModification.getInteger("type")),
                            PublicAccount.createFromPublicKey(
                                multisigModification.getString("cosignatoryPublicKey"),
                                networkType)))
                .collect(Collectors.toList())
            : Collections.emptyList();

    return new ModifyMultisigAccountTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        transaction.getInteger("minApprovalDelta").byteValue(),
        transaction.getInteger("minRemovalDelta").byteValue(),
        modifications,
        signature,
        signer,
        transactionInfo);
  }
}

/** Aggregate transaction mapper. */
class AggregateTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to aggregate transaction.
   *
   * @param jsonObject Json object.
   * @return Aggregate transaction.
   */
  @Override
  public AggregateTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);

    List<AggregateTransactionCosignature> cosignatures = new ArrayList<>();
    if (transaction.getJsonArray("cosignatures") != null) {
      cosignatures =
          transaction.getJsonArray("cosignatures").stream()
              .map(item -> (JsonObject) item)
              .map(
                  aggregateCosignature ->
                      new AggregateTransactionCosignature(
                          aggregateCosignature.getString("signature"),
                          new PublicAccount(
                              aggregateCosignature.getString("signerPublicKey"), networkType)))
              .collect(Collectors.toList());
    }

    return new AggregateTransaction(
        networkType,
        TransactionType.rawValueOf(transaction.getInteger("type").shortValue()),
        version,
        deadline,
        maxFee,
        new ArrayList<>(),
        cosignatures,
        signature,
        signer,
        transactionInfo);
  }
}

/** Lock funds transaction mapper. */
class LockFundsTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to lock funds transaction.
   *
   * @param jsonObject Json object.
   * @return Lock funds transaction.
   */
  @Override
  public LockFundsTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    final MosaicId mosaicId = new MosaicId(extractBigInteger(transaction, "mosaicId"));
    final BigInteger amount = BigInteger.valueOf(transaction.getInteger("amount"));
    return new LockFundsTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        new Mosaic(mosaicId, amount),
        extractBigInteger(transaction, "duration"),
        new SignedTransaction("", transaction.getString("hash"), TransactionType.AGGREGATE_BONDED),
        signature,
        signer,
        transactionInfo);
  }
}

/** Secret lock transaction mapper. */
class SecretLockTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to secret lock transaction.
   *
   * @param jsonObject Json object.
   * @return Secret lock transaction.
   */
  @Override
  public SecretLockTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    return new SecretLockTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        new MosaicMapper().apply(transaction),
        extractBigInteger(transaction, "duration"),
        HashType.rawValueOf(transaction.getInteger("hashAlgorithm")),
        transaction.getString("secret"),
        Address.createFromEncoded(transaction.getString("recipientAddress")),
        signature,
        signer,
        transactionInfo);
  }
}

/** Secret proof transaction mapper. */
class SecretProofTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to secret proof transaction.
   *
   * @param jsonObject Json object.
   * @return Secret proof transaction.
   */
  @Override
  public SecretProofTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    return new SecretProofTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        HashType.rawValueOf(transaction.getInteger("hashAlgorithm")),
        Address.createFromEncoded(transaction.getString("recipientAddress")),
        transaction.getString("secret"),
        transaction.getString("proof"),
        signature,
        signer,
        transactionInfo);
  }
}

/** Mosaic alias transaction mapper. */
class MosaicAliasTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to mosaic alias transaction.
   *
   * @param jsonObject Json object.
   * @return Mosaic alias transaction.
   */
  @Override
  public MosaicAliasTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    return new MosaicAliasTransaction(
        networkType,
        version,
        deadline,
        maxFee,
        AliasAction.rawValueOf(transaction.getInteger("aliasAction").byteValue()),
        new NamespaceId(extractBigInteger(transaction, "namespaceId")),
        new MosaicId(extractBigInteger(transaction, "mosaicId")),
        signature,
        signer,
        transactionInfo);
  }
}

/** Address alias transaction mapper. */
class AddressAliasTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to Address alias transaction.
   *
   * @param jsonObject Json object.
   * @return Address alias transaction.
   */
  @Override
  public AddressAliasTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    return new AddressAliasTransaction(
            networkType,
            version,
            deadline,
            maxFee,
            AliasAction.rawValueOf(transaction.getInteger("aliasAction").byteValue()),
            new NamespaceId(extractBigInteger(transaction, "namespaceId")),
            Address.createFromEncoded(transaction.getString("address")),
            signature,
            signer,
            transactionInfo);
  }
}

  /** Account mosaic restriction modification alias transaction mapper. */
  class AccountMosaicRestrictionModificationTransactionMapper extends TransactionMapper {
    /**
     * Converts from json to Account mosaic restriction modification alias transaction.
     *
     * @param jsonObject Json object.
     * @return Address alias transaction.
     */
    @Override
    public AccountMosaicRestrictionModificationTransaction apply(final JsonObject jsonObject) {
      extractCommonProperties(jsonObject);
      List<AccountRestrictionModification<MosaicId>> modifications = transaction.getJsonArray("modifications").stream()
              .map(item -> (JsonObject) item)
              .map(modification ->
                      AccountRestrictionModification.createForMosaic(
                              AccountRestrictionModificationType.rawValueOf(
                                      modification.getInteger("modificationAction").byteValue()),
                              new MosaicId(extractBigInteger(modification, "value"))
                      ))
              .collect(Collectors.toList());
      return new AccountMosaicRestrictionModificationTransaction(
              networkType,
              version,
              deadline,
              maxFee,
              AccountRestrictionType.rawValueOf(transaction.getInteger("restrictionType").byteValue()),
              modifications,
              signature,
              signer,
              transactionInfo);
    }
  }

  /** Account address restriction modification alias transaction mapper. */
  class AccountAddressRestrictionModificationTransactionMapper extends TransactionMapper {
  /**
   * Converts from json to Account address restriction modification alias transaction.
   *
   * @param jsonObject Json object.
   * @return Address alias transaction.
   */
  @Override
  public AccountAddressRestrictionModificationTransaction apply(final JsonObject jsonObject) {
    extractCommonProperties(jsonObject);
    List<AccountRestrictionModification<Address>> modifications = transaction.getJsonArray("modifications").stream()
            .map(item -> (JsonObject) item)
            .map(modification ->
                    AccountRestrictionModification.createForAddress(
                            AccountRestrictionModificationType.rawValueOf(
                                    modification.getInteger("modificationAction").byteValue()),
                            new Address(modification.getString("value"), networkType))
                    )
            .collect(Collectors.toList());
    return new AccountAddressRestrictionModificationTransaction(
            networkType,
            version,
            deadline,
            maxFee,
            AccountRestrictionType.rawValueOf(transaction.getInteger("restrictionType").byteValue()),
            modifications,
            signature,
            signer,
            transactionInfo);
    }
  }

  /** Account transaction type restriction modification alias transaction mapper. */
  class AccountOperationRestrictionModificationTransactionMapper extends TransactionMapper {
    /**
     * Converts from json to Account transaction type restriction modification alias transaction.
     *
     * @param jsonObject Json object.
     * @return Address alias transaction.
     */
    @Override
    public AccountOperationRestrictionModificationTransaction apply(final JsonObject jsonObject) {
      extractCommonProperties(jsonObject);
      List<AccountRestrictionModification<TransactionType>> modifications = transaction.getJsonArray("modifications").stream()
              .map(item -> (JsonObject) item)
              .map(modification ->
                      AccountRestrictionModification.createForEntityType(
                              AccountRestrictionModificationType.rawValueOf(
                                      modification.getInteger("modificationAction").byteValue()),
                              TransactionType.rawValueOf(modification.getInteger("value").shortValue())
              ))
              .collect(Collectors.toList());
      return new AccountOperationRestrictionModificationTransaction(
              networkType,
              version,
              deadline,
              maxFee,
              AccountRestrictionType.rawValueOf(transaction.getInteger("restrictionType").byteValue()),
              modifications,
              signature,
              signer,
              transactionInfo);
    }
  }