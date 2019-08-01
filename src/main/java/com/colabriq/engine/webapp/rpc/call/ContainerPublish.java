package com.colabriq.engine.webapp.rpc.call;

import com.colabriq.engine.backend.Weft;
import com.colabriq.proto.DHTProto.ContainerPublishRequest;
import com.colabriq.proto.DHTProto.ContainerPublishResponse;
import com.colabriq.rpclib.server.receiver.RPCReceiverSingleResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContainerPublish extends RPCReceiverSingleResponse<ContainerPublishRequest, ContainerPublishResponse> {
	@Inject
	public ContainerPublish(Weft weft) {
		super(
			ContainerPublishRequest.class,
			request -> ContainerPublishResponse
				.newBuilder()
				.setLocation(weft.publishContainer(request.getId(), request.getData().toByteArray()))
				.build()
		);
	}
}
