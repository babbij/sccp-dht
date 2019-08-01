package com.colabriq.engine.webapp.rpc.call;

import com.colabriq.engine.backend.Weft;
import com.colabriq.proto.DHTProto.ContainerFetchRequest;
import com.colabriq.proto.DHTProto.ContainerFetchResponse;
import com.colabriq.rpclib.server.receiver.RPCReceiverSingleResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;

@Singleton
public class ContainerFetch extends RPCReceiverSingleResponse<ContainerFetchRequest, ContainerFetchResponse> {
	@Inject
	public ContainerFetch(Weft weft) {
		super(
			ContainerFetchRequest.class,
			request -> 
				weft.fetchContainer(request.getLocation())
					.map(bytes -> ContainerFetchResponse.newBuilder().setData(ByteString.copyFrom(bytes)).build())
					.orElse(ContainerFetchResponse.newBuilder().build())
		);
	}
}
