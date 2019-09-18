package io.nem.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.namespace.NamespaceId;
import io.nem.sdk.model.receipt.*;
import io.vertx.core.json.JsonObject;

import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionStatementsMapper implements Function<JsonObject, TransactionStatement> {
	final NetworkType networkType;

	/**
	 * Constructor.
	 *
	 * @param networkType Network type.
	 */
	public TransactionStatementsMapper(final NetworkType networkType) {
		this.networkType = networkType;
	}

	/**
	 * Converts a json object to transaction statement
	 *
	 * @param jsonObject Json object.
	 * @return Transaction statement.
	 */
	@Override
	public TransactionStatement apply(final JsonObject jsonObject) {
		return createTransactionStatement(jsonObject.getJsonObject("statement"), networkType);
	}

	public TransactionStatement createTransactionStatement(
			final JsonObject statementJsonObject, NetworkType networkType) {
		final JsonObject sourceJsonObject = statementJsonObject.getJsonObject("source");
		return new TransactionStatement(
				MapperUtils.extractBigInteger(statementJsonObject, "height"),
				new ReceiptSource(
						sourceJsonObject.getInteger("primaryId"),
						sourceJsonObject.getInteger("secondaryId")),
				statementJsonObject.getJsonArray("receipts").stream()
						.map(receipt -> createReceipt((JsonObject) receipt, networkType))
						.collect(Collectors.toList()));
	}

	public Receipt createReceipt(final JsonObject receiptJsonObject, NetworkType networkType) {
		ReceiptType type = ReceiptType.rawValueOf(receiptJsonObject.getInteger("type"));
		switch (type) {
			case Harvest_Fee:
			case LockHash_Created:
			case LockHash_Completed:
			case LockHash_Expired:
			case LockSecret_Created:
			case LockSecret_Completed:
			case LockSecret_Expired:
				return createBalanceChangeReceipt(receiptJsonObject, type, networkType);
			case Mosaic_Rental_Fee:
			case Namespace_Rental_Fee:
				return createBalanceTransferRecipient(receiptJsonObject, type, networkType);
			case Mosaic_Expired:
				return createArtifactExpiryReceipt(receiptJsonObject, type, (final BigInteger id) -> new MosaicId(id));
			case Namespace_Deleted:
			case Namespace_Expired:
				return createArtifactExpiryReceipt(receiptJsonObject, type, (final BigInteger id) -> new NamespaceId(id));
			case Inflation:
				return createInflationReceipt(receiptJsonObject, type);
			default:
				throw new IllegalArgumentException("Receipt type: " + type.name() + " not valid");
		}
	}

	private <T> ArtifactExpiryReceipt<T> createArtifactExpiryReceipt(
			final JsonObject receiptJsonObject,
			final ReceiptType type,
			final Function<BigInteger, T> factory) {
		return new ArtifactExpiryReceipt<T>(
				factory.apply(MapperUtils.extractBigInteger(receiptJsonObject, "artifactId")),
				type,
				ReceiptVersion.ARTIFACT_EXPIRY);
	}

	private BalanceChangeReceipt createBalanceChangeReceipt(
			final JsonObject receiptJsonObject,
			final ReceiptType type,
			final NetworkType networkType) {
		return new BalanceChangeReceipt(
				PublicAccount.createFromPublicKey(
						receiptJsonObject.getString("targetPublicKey"),
						networkType),
				new MosaicId(MapperUtils.extractBigInteger(receiptJsonObject, "mosaicId")),
				MapperUtils.extractBigInteger(receiptJsonObject, "amount"),
				type,
				ReceiptVersion.BALANCE_CHANGE);
	}

	private BalanceTransferReceipt<Address> createBalanceTransferRecipient(
			final JsonObject receiptJsonObject,
			final ReceiptType type,
			final NetworkType networkType) {
		return new BalanceTransferReceipt<>(
				PublicAccount.createFromPublicKey(receiptJsonObject.getString("senderPublicKey"), networkType),
				Address.createFromEncoded(receiptJsonObject.getString("recipientAddress")),
				new MosaicId(MapperUtils.extractBigInteger(receiptJsonObject, "mosaicId")),
				MapperUtils.extractBigInteger(receiptJsonObject, "amount"),
				type,
				ReceiptVersion.BALANCE_TRANSFER);
	}

	private InflationReceipt createInflationReceipt(final JsonObject receiptJsonObject, final ReceiptType type) {
		return new InflationReceipt(
				new MosaicId(MapperUtils.extractBigInteger(receiptJsonObject, "mosaicId")),
				MapperUtils.extractBigInteger(receiptJsonObject, "amount"),
				type,
				ReceiptVersion.INFLATION_RECEIPT);
	}
}
