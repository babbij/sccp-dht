//package com.goodforgoodbusiness.engine.backend;
//
//import com.goodforgoodbusiness.engine.backend.impl.MemDHTBackend;
//import com.goodforgoodbusiness.engine.backend.impl.MongoDHTBackend;
//import com.goodforgoodbusiness.engine.backend.impl.remote.RemoteDHTBackend;
//
//public enum DHTBackendOption {
//	MEMORY(MemDHTBackend.class),
//	MONGO(MongoDHTBackend.class),
//	REMOTE(RemoteDHTBackend.class)
//	
//	;
//	
//	private final Class<? extends DHTBackend> implClass;
//
//	private DHTBackendOption(Class<? extends DHTBackend> implClass) {
//		this.implClass = implClass;
//	}
//
//	public Class<? extends DHTBackend> getImplClass() {
//		return implClass;
//	}
//}
