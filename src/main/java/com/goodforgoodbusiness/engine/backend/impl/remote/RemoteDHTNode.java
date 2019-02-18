package com.goodforgoodbusiness.engine.backend.impl.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface RemoteDHTNode extends Remote {	
	/**
	 * Searches the backend with a keyword as specified to publish.
	 * Returns a Set of Strings representing things that can be retrieved.
	 * Representation is implementation-specific.
	 */
	public Set<String> search(String keyword) throws RemoteException;
	
	/**
	 * Fetches published data based on its location as returned 
	 * from publish or search operations.
	 */
	public String fetch(String location) throws RemoteException;
}
