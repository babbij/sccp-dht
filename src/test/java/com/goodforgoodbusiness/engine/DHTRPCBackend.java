package com.goodforgoodbusiness.engine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.engine.backend.Warp;
import com.goodforgoodbusiness.engine.backend.Weft;
import com.goodforgoodbusiness.proto.DHTProto.ContainerFetchRequest;
import com.goodforgoodbusiness.proto.DHTProto.ContainerFetchResponse;
import com.goodforgoodbusiness.proto.DHTProto.ContainerPublishRequest;
import com.goodforgoodbusiness.proto.DHTProto.ContainerPublishResponse;
import com.goodforgoodbusiness.proto.DHTProto.ContainerSearchRequest;
import com.goodforgoodbusiness.proto.DHTProto.ContainerSearchResponse;
import com.goodforgoodbusiness.proto.DHTProto.PointerPublishRequest;
import com.goodforgoodbusiness.proto.DHTProto.PointerPublishResponse;
import com.goodforgoodbusiness.proto.DHTProto.PointerSearchRequest;
import com.goodforgoodbusiness.proto.DHTProto.PointerSearchResponse;
import com.goodforgoodbusiness.rpclib.client.RPCClient;
import com.goodforgoodbusiness.rpclib.client.RPCClientException;
import com.goodforgoodbusiness.rpclib.client.response.RPCResponse;
import com.goodforgoodbusiness.rpclib.client.response.RPCSingleResponseHandler;
import com.goodforgoodbusiness.rpclib.client.response.RPCStreamResponseHandler;
import com.goodforgoodbusiness.shared.URIModifier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.ByteString;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

/**
 * Implements {@link Warp} / {@link Weft} using the client to go across to dhtengine
 */
@Singleton
public class DHTRPCBackend implements DHTBackend {
	private static final Logger log = Logger.getLogger(DHTRPCBackend.class);
	
	private final Vertx vertx;
	private final RPCClient client;
	
	@Inject
	public DHTRPCBackend(Vertx vertx, WebClient client, @Named("dht.url") String dhtURL) throws URISyntaxException {
		this.vertx = vertx;
		this.client = new RPCClient(vertx, client, URIModifier.from(new URI(dhtURL)).appendPath("/rpc").build());
	}
	
	@Override
	public void publishPointer(String pattern, byte[] data, Future<Void> future) {
		var blocker = new SynchronousQueue<AsyncResult<RPCResponse<PointerPublishResponse>>>();
		
		client.send(
			PointerPublishRequest.newBuilder()
				.setPattern(pattern)
				.setData(ByteString.copyFrom(data))
				.build()
			,
			new RPCSingleResponseHandler<>(
				vertx,
				PointerPublishResponse.class,
				Future.<RPCResponse<PointerPublishResponse>>future().setHandler(result -> blocker.offer(result))
			)
		);
		
		try {
			var result = blocker.take();
			if (result.succeeded()) {
				future.complete(null);
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (InterruptedException e) {
			future.fail(e);
		}
	}

	@Override
	public void searchForPointers(String pattern, Future<Stream<byte[]>> future) {
		var blocker = new SynchronousQueue<AsyncResult<Stream<RPCResponse<PointerSearchResponse>>>>();
		
		client.send(
			PointerSearchRequest.newBuilder()
				.setPattern(pattern)
				.build()
			,
			new RPCStreamResponseHandler<>(
				vertx,
				PointerSearchResponse.class,
				Future.<Stream<RPCResponse<PointerSearchResponse>>>future().setHandler(result -> blocker.offer(result))
			)
		);
		
		try {
			var result = blocker.take();
			if (result.succeeded()) {
				future.complete(result.result().map(rr -> {
					try {
						return rr.get().getResponse().toByteArray();
					}
					catch (RPCClientException e) {
						log.error(e);
						return null;
					}
				}).filter(Objects::nonNull));
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (InterruptedException e) {
			future.fail(e);
		}		
	}

	@Override
	public void publishContainer(String id, byte[] data, Future<String> future) {
		var blocker = new SynchronousQueue<AsyncResult<RPCResponse<ContainerPublishResponse>>>();
		
		client.send(
			ContainerPublishRequest.newBuilder()
				.setId(id)
				.setData(ByteString.copyFrom(data))
				.build()
			,
			new RPCSingleResponseHandler<>(
				vertx,
				ContainerPublishResponse.class,
				Future.<RPCResponse<ContainerPublishResponse>>future().setHandler(result -> blocker.offer(result))
			)
		);

		try {
			var result = blocker.take();
			if (result.succeeded()) {
				future.complete(result.result().get().getLocation());
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (RPCClientException e) {
			future.fail(e);
		}
		catch (InterruptedException e) {
			future.fail(e);
		}
	}

	@Override
	public void searchForContainer(String id, Future<Stream<String>> future) {
		var blocker = new SynchronousQueue<AsyncResult<Stream<RPCResponse<ContainerSearchResponse>>>>();
		
		client.send(
			ContainerSearchRequest.newBuilder()
				.setId(id)
				.build()
			,
			new RPCStreamResponseHandler<>(
				vertx,
				ContainerSearchResponse.class,
				Future.<Stream<RPCResponse<ContainerSearchResponse>>>future().setHandler(result -> blocker.offer(result))
			)
		);
		
		try {
			var result = blocker.take();
			if (result.succeeded()) {
				future.complete(result.result().map(rr -> {
					try {
						return rr.get().getLocation();
					}
					catch (RPCClientException e) {
						log.error(e);
						return null;
					}
				}).filter(Objects::nonNull));
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (InterruptedException e) {
			future.fail(e);
		}
	}

	@Override
	public void fetchContainer(String location, Future<Optional<byte[]>> future) {
		var blocker = new SynchronousQueue<AsyncResult<RPCResponse<ContainerFetchResponse>>>();
		
		client.send(
			ContainerFetchRequest.newBuilder()
				.setLocation(location)
				.build()
			,
			new RPCSingleResponseHandler<>(
				vertx,
				ContainerFetchResponse.class,
				Future.<RPCResponse<ContainerFetchResponse>>future().setHandler(result -> blocker.offer(result))
			)
		);
		
		try {
			var result = blocker.take();
			if (result.succeeded()) {
				future.complete(Optional.of(result.result().get().getData().toByteArray()));
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (RPCClientException e) {
			future.fail(e);
		}
		catch (InterruptedException e) {
			future.fail(e);
		}
	}

	public void close() {
	}
}

