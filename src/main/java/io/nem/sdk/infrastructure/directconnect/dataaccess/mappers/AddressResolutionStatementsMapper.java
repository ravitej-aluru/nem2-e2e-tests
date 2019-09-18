package io.nem.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.receipt.ReceiptType;
import io.nem.sdk.model.receipt.ResolutionStatement;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class AddressResolutionStatementsMapper implements Function<JsonObject, ResolutionStatement<Address>> {
	/**
	 * Converts a json object to resolution statement
	 *
	 * @param jsonObject Json object.
	 * @return Resolution statement.
	 */
	@Override
	public ResolutionStatement<Address> apply(final JsonObject jsonObject) {
		return createAddressResolutionStatement(jsonObject.getJsonObject("statement"));
	}

	private ResolutionStatement<Address> createAddressResolutionStatement(
			final JsonObject receiptJsonObject) {
		return MapperUtils.<Address>createResolutionStatement(receiptJsonObject,
				(final JsonObject jsonObject) -> Address.createFromEncoded(jsonObject.getString("unresolved")),
				(final JsonObject jsonObject) -> Address.createFromEncoded(jsonObject.getString("resolved")),
				ReceiptType.Address_Alias_Resolution);
	}
}