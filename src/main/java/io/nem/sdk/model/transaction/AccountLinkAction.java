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

/** Account link action. */
public enum AccountLinkAction {
  /** Unlink account. */
  UNLINK((byte) 0),
  /** Link account. */
  LINK((byte) 1);

  private final byte value;

  /**
   * Constructor.
   *
   * @param value Link action value.
   */
  AccountLinkAction(final byte value) {
    this.value = value;
  }

  /**
   * Gets enum value from raw.
   *
   * @param value Raw value.
   * @return Enum value.
   */
  public static AccountLinkAction rawValueOf(final int value) {
    switch (value) {
      case 0:
        return AccountLinkAction.UNLINK;
      case 1:
        return AccountLinkAction.LINK;
      default:
        throw new IllegalArgumentException(value + " is not a valid value");
    }
  }

  /**
   * Gets the raw value.
   *
   * @return Ram value.
   */
  public byte getValue() {
    return value;
  }
}
