package com.goodforgoodbusiness.engine.webapp.rpc.call;

import com.goodforgoodbusiness.engine.backend.Weft;
import com.goodforgoodbusiness.proto.DHTProto.ContainerPublishRequest;
import com.goodforgoodbusiness.proto.DHTProto.ContainerPublishResponse;
import com.goodforgoodbusiness.rpclib.server.receiver.RPCReceiverSingleResponse;
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
