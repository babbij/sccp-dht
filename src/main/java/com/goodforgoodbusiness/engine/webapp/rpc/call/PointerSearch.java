package com.goodforgoodbusiness.engine.webapp.rpc.call;

import com.goodforgoodbusiness.engine.backend.Warp;
import com.goodforgoodbusiness.proto.DHTProto.PointerSearchRequest;
import com.goodforgoodbusiness.proto.DHTProto.PointerSearchResponse;
import com.goodforgoodbusiness.rpclib.server.receiver.RPCReceiverStreamResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;

@Singleton
public class PointerSearch extends RPCReceiverStreamResponse<PointerSearchRequest, PointerSearchResponse> {
	@Inject
	public PointerSearch(Warp warp) {
		super(
			PointerSearchRequest.class,
			request -> 
				warp.searchForPointers(request.getPattern()).map(
					result -> PointerSearchResponse
						.newBuilder()
						.setResponse(ByteString.copyFrom(result))
						.build()
				)
		);
	}
}
