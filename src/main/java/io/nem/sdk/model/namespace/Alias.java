/*
 * Copyright 2019 NEM
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

package io.nem.sdk.model.namespace;

/**
 * The alias structure defines an interface for Aliases
 */
public interface Alias<T> {

	/**
	 * Gets the alias type
	 *
	 * @return aliasType {@link AliasType}
	 */
	AliasType getType();

	/**
	 * Gets the alias.
	 *
	 * @return Alias of type T.
	 */
	T getAliasValue();

	/**
	 * Compares this alias to specified alias.
	 *
	 * @param alias
	 * @return true if aliases are of the same type; false otherwise
	 */
	boolean equals(Alias<T> alias);

	/**
	 * Returns true if this alias is anEmptyAlias; false otherwise.
	 *
	 * @param
	 * @return true if alias is empty; false otherwise
	 */
	boolean isEmpty();
}