/*
 * Copyright (c) 2016-present,
 * Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
 *
 * This file is part of Catapult.
 *
 * Catapult is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Catapult is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Catapult.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.nem.symbol.sdk.infrastructure.directconnect.network;

import io.nem.symbol.core.crypto.KeyPair;
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.sdk.infrastructure.directconnect.auth.ConnectionSecurityMode;
import io.nem.symbol.sdk.infrastructure.directconnect.auth.VerifyServer;

/** Authenticated socket to the catapult server. */
public class AuthenticatedSocket {
  private static AuthenticatedSocket authenticatedSocket = null;
  /* Key pair value use in the server challenge */
  final KeyPair keyPair;
  /* Client socket. */
  final SocketClient socketClient;

  /**
   * Constructor
   *
   * @param socket Client socket.
   * @param publicKey Server public key.
   */
  private AuthenticatedSocket(
      final SocketClient socket, final PublicKey publicKey, final KeyPair keyPair) {
    this.keyPair = keyPair;

    final VerifyServer verifyServer =
        new VerifyServer(socket, keyPair, publicKey, ConnectionSecurityMode.NONE);
    verifyServer.verifyConnection();
    this.socketClient = socket;
  }

  /**
   * Creates an authenticated socket with the server.
   *
   * @param socketClient Socket connection to the server.
   * @param publicKey Server public key
   * @param clientKeyPair Client key pair
   * @return Authenticated socket
   */
  public static AuthenticatedSocket create(
      final SocketClient socketClient, final PublicKey publicKey, final KeyPair clientKeyPair) {
    return new AuthenticatedSocket(socketClient, publicKey, clientKeyPair);
  }

  /**
   * Gets the client socket.
   *
   * @return Socket client.
   */
  public SocketClient getSocketClient() {
    return socketClient;
  }
}