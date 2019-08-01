package com.colabriq.engine.webapp.rpc.call;

import com.colabriq.engine.backend.Warp;
import com.colabriq.proto.DHTProto.PointerSearchRequest;
import com.colabriq.proto.DHTProto.PointerSearchResponse;
import com.colabriq.rpclib.server.receiver.RPCReceiverStreamResponse;
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
