package com.goodforgoodbusiness.engine.webapp.rpc.call;

import com.goodforgoodbusiness.engine.backend.Weft;
import com.goodforgoodbusiness.proto.DHTProto.ContainerFetchRequest;
import com.goodforgoodbusiness.proto.DHTProto.ContainerFetchResponse;
import com.goodforgoodbusiness.rpclib.server.receiver.RPCReceiverSingleResponse;
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
