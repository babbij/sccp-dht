package com.goodforgoodbusiness.engine.webapp.rpc.call;

import com.goodforgoodbusiness.engine.backend.Warp;
import com.goodforgoodbusiness.proto.DHTProto.PointerPublishRequest;
import com.goodforgoodbusiness.proto.DHTProto.PointerPublishResponse;
import com.goodforgoodbusiness.rpclib.server.receiver.RPCReceiverSingleResponse;
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
