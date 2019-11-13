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

package io.nem.sdk.infrastructure.directconnect.network;

import io.nem.core.utils.ExceptionUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Create a connection with catapult server.
 */
public class SocketFactory {

	/**
	 * Open a socket connection to the given server on the given port.
	 *
	 * @param server                Server connection.
	 * @param port                  Server port.
	 * @param timeoutInMilliseconds Timeout in milliseconds.
	 * @return Client socket
	 */
	public static SocketClient OpenSocket(
			final String server, final int port, final int timeoutInMilliseconds) {
		final Socket socketConnected = ExceptionUtils.propagate(
				() -> {
					final InetAddress inetAddress = InetAddress.getByName(server);
					final SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
					final Socket socket = new Socket();

					// this method will block no more than timeout ms.
					socket.connect(socketAddress, timeoutInMilliseconds);
					return socket;
				});
		return SocketClient.create(socketConnected);
	}
}
