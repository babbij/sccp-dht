package com.colabriq.engine.webapp.rpc.call;

import com.colabriq.engine.backend.Warp;
import com.colabriq.proto.DHTProto.PointerPublishRequest;
import com.colabriq.proto.DHTProto.PointerPublishResponse;
import com.colabriq.rpclib.server.receiver.RPCReceiverSingleResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PointerPublish extends RPCReceiverSingleResponse<PointerPublishRequest, PointerPublishResponse> {
	@Inject
	public PointerPublish(Warp warp) {
		super(
			PointerPublishRequest.class,
			request -> {
				warp.publishPointer(request.getPattern(), request.getData().toByteArray());
				return PointerPublishResponse.newBuilder().build();
			}
		);
	}
}
