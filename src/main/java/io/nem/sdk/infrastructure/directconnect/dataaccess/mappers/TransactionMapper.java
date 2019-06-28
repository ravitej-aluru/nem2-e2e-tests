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

import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.mosaic.*;
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

/**
 * Transaction mapper.
 */
public class TransactionMapper implements Function<JsonObject, Transaction> {
	/* Transaction json. */
	JsonObject transaction;
	/* Transaction deadline. */
	Deadline deadline;
	/* Network type. */
	NetworkType networkType;
	/* Transaction version. */
	Integer version;
	/* Max fee. */
	BigInteger maxFee;
	/* Transaction info. */
	TransactionInfo transactionInfo;
	/* Transaction type. */
	TransactionType type = TransactionType.RESERVED;

	/**
	 * Gets the common properties for all transactions.
	 *
	 * @param jsonObject Json object.
	 */
	protected void extractCommonProperties(final JsonObject jsonObject) {
		if (type == TransactionType.RESERVED) {
			transactionInfo = this.createTransactionInfo(jsonObject.getJsonObject("meta"));
			transaction = jsonObject.getJsonObject("transaction");
			type = TransactionType.rawValueOf(transaction.getInteger("type"));
			deadline = new Deadline(extractBigInteger(transaction, "deadline"));
			networkType = extractNetworkType(transaction.getInteger("version"));
			version = extractTransactionVersion(transaction.getInteger("version"));
			maxFee = extractBigInteger(transaction, "maxFee");
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
		} else if (type == TransactionType.REGISTER_NAMESPACE) {
			return new RegisterNamespaceTransactionMapper().apply(jsonObject);
		} else if (type == TransactionType.MOSAIC_DEFINITION) {
			return new MosaicDefinitionTransactionMapper().apply(jsonObject);
		} else if (type == TransactionType.MOSAIC_SUPPLY_CHANGE) {
			return new MosaicSupplyChangeTransactionMapper().apply(jsonObject);
		} else if (type == TransactionType.MODIFY_MULTISIG_ACCOUNT) {
			return new MultisigModificationTransactionMapper().apply(jsonObject);
		} else if (type == TransactionType.AGGREGATE_COMPLETE || type == TransactionType.AGGREGATE_BONDED) {
			return new AggregateTransactionMapper().apply(jsonObject);
		} else if (type == TransactionType.LOCK) {
			return new LockFundsTransactionMapper().apply(jsonObject);
		} else if (type == TransactionType.SECRET_LOCK) {
			return new SecretLockTransactionMapper().apply(jsonObject);
		} else if (type == TransactionType.SECRET_PROOF) {
			return new SecretProofTransactionMapper().apply(jsonObject);
		}

		throw new UnsupportedOperationException("Unimplemented Transaction type");
	}

	/**
	 * Gets a BigInteger from the json.
	 *
	 * @param jsonObject Json object.
	 * @param name       Property name.
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
		if (jsonObject.containsKey("hash") && jsonObject.containsKey("id")) {
			return TransactionInfo.create(extractBigInteger(jsonObject, "height"),
					jsonObject.getInteger("index"),
					jsonObject.getString("id"),
					jsonObject.getString("hash"),
					jsonObject.getString("merkleComponentHash"));
		} else if (jsonObject.containsKey("aggregateHash") && jsonObject.containsKey("id")) {
			return TransactionInfo.createAggregate(extractBigInteger(jsonObject, "height"),
					jsonObject.getInteger("index"),
					jsonObject.getString("id"),
					jsonObject.getString("aggregateHash"),
					jsonObject.getString("aggregateId"));
		} else {
			return TransactionInfo.create(extractBigInteger(jsonObject, "height"),
					jsonObject.getString("hash"),
					jsonObject.getString("merkleComponentHash"));
		}
	}
}

/**
 * Transfer transaction mapper.
 */
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
			mosaics = transaction
					.getJsonArray("mosaics")
					.stream()
					.map(item -> (JsonObject) item)
					.map(new MosaicMapper())
					.collect(Collectors.toList());
		}

		Message message = PlainMessage.Empty;
		if (transaction.getJsonObject("message") != null) {
			message = new PlainMessage(new String(Hex.decode(transaction.getJsonObject("message").getString("payload")),
					StandardCharsets.UTF_8));
		}

		return new TransferTransaction(
				networkType,
				version,
				deadline,
				maxFee,
				Optional.of(Address.createFromEncoded(transaction.getString("recipient"))),
				Optional.empty(),
				mosaics,
				message,
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo);
	}
}

/**
 * Register namespace transaction mapper.
 */
class RegisterNamespaceTransactionMapper extends TransactionMapper {
	/**
	 * Converts from json to register namespace transaction.
	 *
	 * @param jsonObject Json object.
	 * @return Register namespace transaction.
	 */
	@Override
	public RegisterNamespaceTransaction apply(final JsonObject jsonObject) {
		extractCommonProperties(jsonObject);
		final NamespaceType namespaceType = NamespaceType.rawValueOf(transaction.getInteger("namespaceType"));

		return new RegisterNamespaceTransaction(
				networkType,
				version,
				deadline,
				maxFee,
				transaction.getString("name"),
				new NamespaceId(extractBigInteger(transaction, "namespaceId")),
				namespaceType,
				namespaceType == NamespaceType.RootNamespace ? Optional.of(extractBigInteger(transaction, "duration")) : Optional.empty(),
				namespaceType == NamespaceType.SubNamespace ? Optional.of(new NamespaceId(extractBigInteger(transaction, "parentId"))) :
						Optional.empty(),
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

/**
 * Mosaic definition transaction mapper.
 */
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
				MosaicNonce.createFromBigInteger(extractBigInteger(transaction, "mosaicNonce")),
				new MosaicId(extractBigInteger(transaction, "mosaicId")),
				properties,
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

/**
 * Mosaic supply change transaction mapper.
 */
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
				MosaicSupplyType.rawValueOf(transaction.getInteger("direction")),
				extractBigInteger(transaction, "delta"),
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

/**
 * Multisig modification transaction mapper.
 */
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
		final List<MultisigCosignatoryModification> modifications = transaction.containsKey("modifications") ? transaction
				.getJsonArray("modifications")
				.stream()
				.map(item -> (JsonObject) item)
				.map(multisigModification -> new MultisigCosignatoryModification(
						MultisigCosignatoryModificationType.rawValueOf(multisigModification.getInteger("type")),
						PublicAccount.createFromPublicKey(multisigModification.getString("cosignatoryPublicKey"), networkType)))
				.collect(Collectors.toList()) : Collections.emptyList();

		return new ModifyMultisigAccountTransaction(
				networkType,
				version,
				deadline,
				maxFee,
				transaction.getInteger("minApprovalDelta").byteValue(),
				transaction.getInteger("minRemovalDelta").byteValue(),
				modifications,
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

/**
 * Aggregate transaction mapper.
 */
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
			cosignatures = transaction
					.getJsonArray("cosignatures")
					.stream()
					.map(item -> (JsonObject) item)
					.map(aggregateCosignature -> new AggregateTransactionCosignature(
							aggregateCosignature.getString("signature"),
							new PublicAccount(aggregateCosignature.getString("signer"), networkType)))
					.collect(Collectors.toList());
		}

		return new AggregateTransaction(
				networkType,
				TransactionType.rawValueOf(transaction.getInteger("type")),
				version,
				deadline,
				maxFee,
				new ArrayList<>(),
				cosignatures,
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

/**
 * Lock funds transaction mapper.
 */
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
		return new LockFundsTransaction(
				networkType,
				version,
				deadline,
				maxFee,
				new MosaicMapper().apply(transaction),
				extractBigInteger(transaction, "duration"),
				new SignedTransaction("", transaction.getString("hash"), TransactionType.AGGREGATE_BONDED),
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

/**
 * Secret lock transaction mapper.
 */
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
				Address.createFromEncoded(transaction.getString("recipient")),
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

/**
 * Secret proof transaction mapper.
 */
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
				Address.createFromEncoded(transaction.getString("recipient")),
				transaction.getString("secret"),
				transaction.getString("proof"),
				transaction.getString("signature"),
				new PublicAccount(transaction.getString("signer"), networkType),
				transactionInfo
		);
	}
}

