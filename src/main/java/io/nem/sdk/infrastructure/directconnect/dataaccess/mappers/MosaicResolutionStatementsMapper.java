package io.nem.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.namespace.MosaicAlias;
import io.nem.sdk.model.receipt.ReceiptSource;
import io.nem.sdk.model.receipt.ReceiptType;
import io.nem.sdk.model.receipt.ResolutionEntry;
import io.nem.sdk.model.receipt.ResolutionStatement;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MosaicResolutionStatementsMapper implements Function<JsonObject, ResolutionStatement<MosaicId>> {
	/**
	 * Converts a json object to resolution statement
	 *
	 * @param jsonObject Json object.
	 * @return Resolution statement.
	 */
	@Override
	public ResolutionStatement<MosaicId> apply(final JsonObject jsonObject) {
		return createMosaicResolutionStatement(jsonObject.getJsonObject("statement"));
	}

	private ResolutionStatement<MosaicId> createMosaicResolutionStatement(
			final JsonObject receiptJsonObject) {
		return MapperUtils.<MosaicId>createResolutionStatement(receiptJsonObject,
				(final JsonObject jsonObject) -> new MosaicId(MapperUtils.extractBigInteger(jsonObject, "unresolved")),
				(final JsonObject jsonObject) -> new MosaicId(MapperUtils.extractBigInteger(jsonObject, "resolved")),
				ReceiptType.MOSAIC_ALIAS_RESOLUTION);
	}
}
