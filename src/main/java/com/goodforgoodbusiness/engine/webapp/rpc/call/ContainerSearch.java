package com.goodforgoodbusiness.engine.webapp.rpc.call;

import com.goodforgoodbusiness.engine.backend.Weft;
import com.goodforgoodbusiness.proto.DHTProto.ContainerSearchRequest;
import com.goodforgoodbusiness.proto.DHTProto.ContainerSearchResponse;
import com.goodforgoodbusiness.rpclib.server.receiver.RPCReceiverStreamResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContainerSearch extends RPCReceiverStreamResponse<ContainerSearchRequest, ContainerSearchResponse> {
	@Inject
	public ContainerSearch(Weft weft) {
		super(
			ContainerSearchRequest.class,
			request -> 
				weft.searchForContainer(request.getId()).map(
					result -> ContainerSearchResponse.newBuilder()
						.setLocation(result)
						.build()
				)
		);
	}
}
