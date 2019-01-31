package com.goodforgoodbusiness.engine.dht.impl.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import com.goodforgoodbusiness.dht.Participant;

public interface DHTNode extends Remote, Participant {
	Set<String> getPointers(String pattern) throws RemoteException;
	String getClaim(String id) throws RemoteException;
}
