package com.goodforgoodbusiness.engine.dht.impl.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface RemoteDHTNode extends Remote {
	public Set<String> getPointers(String pattern) throws RemoteException;
	public String getClaim(String id) throws RemoteException;
}
