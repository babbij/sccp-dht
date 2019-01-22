package com.goodforgoodbusiness.dhtjava;

import static java.lang.System.getenv;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.goodforgoodbusiness.dhtjava.dht.DHTStore;
import com.goodforgoodbusiness.dhtjava.dht.impl.MongoDHTStore;
import com.goodforgoodbusiness.dhtjava.dht.share.ShareKeyStore;
import com.goodforgoodbusiness.dhtjava.dht.share.impl.MongoKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEInstance;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.kpabe.key.KPABESecretKey;
import com.google.inject.AbstractModule;

public class DHTApp extends AbstractModule {
	@Override
	protected void configure() {
		bind(DHTStore.class).to(MongoDHTStore.class);
		bind(ShareKeyStore.class).to(MongoKeyStore.class);
		bind(KPABEInstance.class);
	}
	
	private static final int DHT_PORT = toInt(getenv("DHT_PORT"), 8090);
	
	// temporary....
	public static final KPABEInstance kpabe;
	
	static {
		try {
			kpabe = KPABEInstance.forKeys(
				new KPABEPublicKey(
					"AAAAFqpvypNchY4yd2g2x43SJvGQSt5tcGsAAAGooQFZsgEEtLIBABUPDm5AJ47WcfaRZiN1CcduNSnTJ9GwEGOozGKIBcpZ" + 
					"EoKUr6pr4v62jiS5jXh09M2XmmgmwnIivftBWhV2U7cZUEubwEe6uZxFXwupC6mXxFaSiUTCBes0Eryc7CKz1g50/gkJguQi" + 
					"Wxat/R+hlpnaQDQi9JOPb8tnKls63FIzBareEPqHU+Dgki/IVy8GjNkwz+hQjQUPSzofR5J4+i4HABo/xg8OYA3/qxW0HqDV" + 
					"7JAS3dyJxvVdSA04RvOwqxG9LYFqZi7bN4kjIE66orrXrw8eGh8/m5INA4VKKN2hDSjHImylnq+R9+olS5JXAt4wEFwedOeT" + 
					"mFeXs25pGn6hAmcxoSSyoSEDDYoKFosVFq3O55lowcLe8YVvTRKSGIjseQOQCDD98V+hAmcyoUSzoUEDIPkips5KTh4tmewC" + 
					"GO5m5USr2qDM37xc8hPinDZk5O8iinFs3/+tOmFdo5i9tw8thwGR/gi+00OvDrr77F2uiKEBa6ElHQAAACBz993m0PGKOmhX" + 
					"IGd14UE6c37DX2sGffzCGhgxk6EWvQ=="),
				
				new KPABESecretKey(
					"AAAAFqpvyqDHFEgVlg03qvxVFa9EMS9tc2sAAAAooQF5oSOxACAYi1mFdjwtsOCnNQaiv1PDtZGDdLkRdgdpVtUi41GATQ==")
			);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
//		var dhtService = new DHTService(
//			DHT_PORT,
//			new MongoDHTStore("mongodb://localhost:27017/claims"),
//			new MongoKeyStore("mongodb://localhost:27017/keys"),
//			new Crypto(abe)
//		);
//		
//		dhtService.start();
	}
}
