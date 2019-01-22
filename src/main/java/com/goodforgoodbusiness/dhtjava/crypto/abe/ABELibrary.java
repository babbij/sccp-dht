package com.goodforgoodbusiness.dhtjava.crypto.abe;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

interface ABELibrary extends Library {
	static class COutString extends Structure {
		public static class ByReference extends COutString implements Structure.ByReference {}
		
		public int cb;
		public Pointer sz;
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("cb", "sz");
		}
		
		@Override
		public String toString() {
			try {
				return new String(sz.getByteArray(0, cb), "US-ASCII");
			}
			catch (UnsupportedEncodingException e) {
				throw new RuntimeException("US-ASCII is not supported");
			}
		}
	}
	
	static class CKeyPair extends Structure implements Closeable {
		public static class ByReference extends CKeyPair implements Structure.ByReference {}
		
		public Pointer szPublicKey;
		public Pointer szSecretKey;
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("szPublicKey", "szSecretKey");
		}
		
		public String getPublicKey() {
			return szPublicKey.getString(0);
		}
		
		public String getSecretKey() {
			return szSecretKey.getString(0);
		}

		@Override
		public void close() {
			// XXX these lines cause 'malloc' errors
			// Native.free(Pointer.nativeValue(szPublicKey));
			// Native.free(Pointer.nativeValue(szSecretKey));
		}
	}
	
	int newKeyPair(CKeyPair.ByReference pKeyPair);
	int encrypt(CKeyPair.ByReference pKeyPair, String szAttrs, String szInput, COutString.ByReference pOutput);
	int shareKey(CKeyPair.ByReference pKeyPair, String szAttrs, COutString.ByReference pOutput);
	int decrypt(String szPublicKey, String szCipher, String szShareKey, COutString.ByReference pOutput);
}
