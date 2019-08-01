package com.colabriq.engine.webapp.rpc.call;

import com.colabriq.engine.backend.Weft;
import com.colabriq.proto.DHTProto.ContainerSearchRequest;
import com.colabriq.proto.DHTProto.ContainerSearchResponse;
import com.colabriq.rpclib.server.receiver.RPCReceiverStreamResponse;
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
