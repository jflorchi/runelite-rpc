package io.paratek.rpc;

import com.google.inject.Provides;
import javax.inject.Inject;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "RPC"
)
public class RPCPlugin extends Plugin {

	@Inject
	private Client client;

	private Server server;

	@Override
	protected void startUp() throws Exception {
		if (this.server == null) {
			this.server = ServerBuilder.forPort(50051)
					.addService(new ClientService(this.client))
					.build();
		}
		log.info("Starting gRPC Server on port 50051");
		this.server.start();
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("Stopping gRPC Server");
		this.server.shutdown();
	}

}
