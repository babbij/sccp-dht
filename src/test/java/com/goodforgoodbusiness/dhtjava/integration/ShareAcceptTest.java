package com.goodforgoodbusiness.dhtjava.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import com.goodforgoodbusiness.shared.ContentType;

public class ShareAcceptTest {
	public static void main(String[] args) throws Exception {
		var json = 
			"{                                     " + 
			"  \"pattern\": {                      " + 
			"    \"sub\": \"test1\",               " + 
			"    \"pre\": null,                    " + 
			"    \"obj\": \"test3\"                " + 
			"  },                                  " + 
			"  \"range\": {                        " + 
			"    \"start\": \"2019-01-01T00:00\",  " + 
			"    \"end\": null                     " + 
			"  },                                  " + 
			"  \"key\": {                          " + 
			"    \"public\": \"AAAAFqpvypNchY4yd2g2x43SJvGQSt5tcGsAAAGooQFZsgEEtLIBABUPDm5AJ47WcfaRZiN1CcduNSnTJ9GwEGOozGKIBcpZEoKUr6pr4v62jiS5jXh09M2XmmgmwnIivftBWhV2U7cZUEubwEe6uZxFXwupC6mXxFaSiUTCBes0Eryc7CKz1g50/gkJguQiWxat/R+hlpnaQDQi9JOPb8tnKls63FIzBareEPqHU+Dgki/IVy8GjNkwz+hQjQUPSzofR5J4+i4HABo/xg8OYA3/qxW0HqDV7JAS3dyJxvVdSA04RvOwqxG9LYFqZi7bN4kjIE66orrXrw8eGh8/m5INA4VKKN2hDSjHImylnq+R9+olS5JXAt4wEFwedOeTmFeXs25pGn6hAmcxoSSyoSEDDYoKFosVFq3O55lowcLe8YVvTRKSGIjseQOQCDD98V+hAmcyoUSzoUEDIPkips5KTh4tmewCGO5m5USr2qDM37xc8hPinDZk5O8iinFs3/+tOmFdo5i9tw8thwGR/gi+00OvDrr77F2uiKEBa6ElHQAAACBz993m0PGKOmhXIGd14UE6c37DX2sGffzCGhgxk6EWvQ==\"," + 
			"    \"share\": \"AAAAFqpvygKr5dgck4BcWEzA+xfFZxVrZXkAAA9uoRJEXzAxODZkOTYxNGU4YjNiMTChJLKhIQMi/2Ae9iOQanCnNp2dYJrkcdkgUpcOlsjCpOr5J5mhkqESRF8wNDBmZDJkZTIyNjllYTY1oSSyoSEDI/epyGDsOcD9QqmWqasgP4PIRHeYrlrxWVAz4lLwn1ShEkRfMGI1NDBiNWUyYzc5NTI4OaEksqEhAxgna4UQLfcxjGL/p5hng3rz9YAwqNZKwPDmZkXLmzcPoRJEXzI3YzllNWE2YjE0ZGU3M2KhJLKhIQIdqGYBlIDZ1JGsqcjeX1FjeG2SDWTCA1AoeVNjBaUeXqESRF8yYTBkNDRkZDNkNThhMmI1oSSyoSECD20wKDSY0UHkNxOrUfb2wLuF5CBph9aqxAMOHCe5NCihEkRfMmQzNThkODk1MTVkOTYxN6EksqEhAhGlCnK2Gzo5AyRWc7vR96LC2h1Xn2igeqta4ktlinxVoRJEXzJlMzJhZjc3MWFhYmQ4ZTihJLKhIQIGuSqwni10YzVcgvt302XRn0/B9cJ+rehJcpkFXCIEuaESRF80ZDVlMWFlNDVkOWFkZjEyoSSyoSECGYnh7hKzL9oIt101pluWODJTXDOeS2U3nW3I0KT/eCShEkRfNTFlYzhjMzM5ZTYwZDQzNKEksqEhAw4WSWjpLJrJ2pwQiny/vVO9MfOFuXEyqC6lGL9yOu3ZoRJEXzYyMzYxOWI3MzIyNTcwNzahJLKhIQMUcsR2M5r7k9L6zDYmZOKslY7CsWIwbYTRRW7W6PIKAKESRF83MTg3ZWI0ZGViYzQyZmE3oSSyoSECEhJ2Dup6QlS9q6ey7+ehWnxNGdVTnYQvp1UXtMlnTHehEkRfNzhiMzU2ZTJkNzBkZTEyM6EksqEhAwkriwq1MWr9b3+ztwgVV4gxOAq0OzIQH7mRyaH0IFiZoRJEXzhjODhkM2Y5MDdmZmY2YzGhJLKhIQMfx041Q+QKCmP9shjXPCCHtBO/5y0DrKJ7z3L9+L14AKESRF84ZjNiMWZjM2M1YjljNWJjoSSyoSEDGLT/bEIpKuX1IPi3tUoStgpo81+SlhY9Ta3l5ybl1yqhEkRfOTI3NTFhZjk1YzNhOTE0MqEksqEhAhKUzk9tLS4TczFuKUS2VXSBOCnnSIzVy87q9ahw8nguoRJEXzk5NTAxMWNlMDhjODU0NmOhJLKhIQMDFjYDqMMKgU68RD06G2UWriy0yHAXaAOsLMBKEgxeOaESRF85ZTk0MzAxODU5NzM1NGNjoSSyoSECIZANMxFWKv7YXvrZpYnf3j8cy49QgDevEd3+3d2KGZyhEkRfYTBlYjdlYTJiMmQzMzlmNKEksqEhAiPLP/RxJ4TKKki65ayXYOjUXevozH6LqTrqHdFp1euroRJEX2EyMDFlZjkxODhiNmViYTmhJLKhIQMK6/9CCPeS1nfZBk2wBZha2LFbLGm5/6PqD8/6L0Wx/6ESRF9hNmEyNmZiODdlZGEzNmMxoSSyoSEDCS6bMJMN4FRwTUxGJYqIeTUQ9Rpqp5774SMAh1zDJDWhEkRfYjdiZTRlNDNiNDNiNTQ4NaEksqEhAwgozgTxi2OTIzoeYKl8a6hv7yLVDHZjWRMYcdSp5hJpoRJEX2NlYWNkZGI4OWNlOTUwNWWhJLKhIQMZ2Kp6jvCpWl7oh2487RgKymvoxWRLAQxQKKFNYwO0KaESRF9kNDQ1YTk2MDQ4ZDBmODk1oSSyoSEDDn53GNOfX+h4Q5O0rDF/pubfjx/B1X6GO0H7ULU/svOhEkRfZGIzN2ViYmI0MjE4ODk1OaEksqEhAg0Fa+NxGZNr38HbSs4kNZD39QXVjmdq2AWjt3zI4FhNoRJEX2VkM2ViZjliNGIyNDEyZTGhJLKhIQIDDBFdKFKPfNAW+5Y5GLf74ZB+Qob4tFFYn9LBz009xaESRF9mMTM3MTQ0YWJkNmMyMjZloSSyoSEDA1uUHmeenaKxQiFJVMKjx5k0PEwG2nDHUaBYZHaoQBihEmRfMDE4NmQ5NjE0ZThiM2IxMKFEs6FBAxYe9sHNH697Rg9a2nnoufRoPralTopEnI24UUlRN6Q2G8czE3pFGRK5TXJvX6T3KkUHUbtyawj/2CuTmtLxfr2hEmRfMDQwZmQyZGUyMjY5ZWE2NaFEs6FBAxwW6TKslQza14XmlIoHDU1rDS7nLB59NlPHar42NXHUAcND9anNMH0crhhwWkKCn4pXV0RFEONSExxryWjhtfihEmRfMGI1NDBiNWUyYzc5NTI4OaFEs6FBAgxsXwQFNI9rEAZTdTD8LFB3qq/xCvbT7SKGb1zASjchIrf82QVL9zWLwpwdWCCEQoOst9JE2VLZgAmBpOxcg8ahEmRfMjdjOWU1YTZiMTRkZTczYqFEs6FBAgO+D4T6Q14kcU3qIqGpSndTuVVSzJCLyTfnTSi5AaCJCSvkBr9apkEtyEfb5m5b/QjCnNf8Dxm/0GjtJNeYRDqhEmRfMmEwZDQ0ZGQzZDU4YTJiNaFEs6FBAwd6SFA6M/I2fd8J2VqIXD+wNKqR7teJBfRw4x8vc/ojDftpNndfkdg2VhVSXpoj2fEo/Bm+/3Blio+SWwwnXtShEmRfMmQzNThkODk1MTVkOTYxN6FEs6FBAhecM2+vt3T0QABa6zZcGWF5XLPU1eME6MgC3NZUL182H7Eme1fshhNqlVyHNSrFSGioHluez636LzGCR15Rj1WhEmRfMmUzMmFmNzcxYWFiZDhlOKFEs6FBAwkPeF3YtKy5GgcUl4o4xaAKan9whSURo+1+dEPQZhO8BFQp1YN697rAGBkCaMZXnvrdxVosQg2C6phQN90XuKuhEmRfNGQ1ZTFhZTQ1ZDlhZGYxMqFEs6FBAws54gKyg/o94oHByiIKtd5wLDzDnccOOg1GPxkRkkUOGVqP40N8fBErumAJa42CkLyfimnaKlEk1YXvW7B/WgGhEmRfNTFlYzhjMzM5ZTYwZDQzNKFEs6FBAweuuq0wOxsGhMpLxtbPACh+54GBgpuao4zl2BkgSJ5UHj6oIdeJ8GhkdrnGOHStGB15HiG7r9AGAk+YiR4UpqChEmRfNjIzNjE5YjczMjI1NzA3NqFEs6FBAh0GFfzCylHK7ozPbzONK1cqnmg3cVqzQdm0y/+d/jDCIDu93JajVkvqG/IqQyCTBR+F9LdauaT5pfFFm09lz0ehEmRfNzE4N2ViNGRlYmM0MmZhN6FEs6FBAgoeV+RFSyhq05508uklxhEYs7nEtGd57YuzJg6cjBdWD+XY8uAK2QrabbKDMzZ7SOrMGELBOrEwzfNmeFUa7OehEmRfNzhiMzU2ZTJkNzBkZTEyM6FEs6FBAwfv9C3PiUkEG1D9nh6OgvIStHV1bAQn6LK0DJ1P1DunE9qyuTqsXLAmU+mhlacsWg0CrbhCekXD67goGKyEAvqhEmRfOGM4OGQzZjkwN2ZmZjZjMaFEs6FBAhxT1F+VIL1hbiKbjyML/+UhF7R+h768DNWrqOeh/dtsCP3ra20WzsWjane4J6xwTJVFoyqYC6kEbDjNxZL5y56hEmRfOGYzYjFmYzNjNWI5YzViY6FEs6FBAgtIWX1Cu+MPffM5XJ2SEoriWzfzpz8P9i/IbKDAkirEFRbxt6q+rB2K6jP3nPqxR4y1un9Y/I/v4fT5YjYFTaShEmRfOTI3NTFhZjk1YzNhOTE0MqFEs6FBAw7peiVsuyTAw74NRHgodFSrsnhhpNPq0MHwiV9IbzUNIfWIGJoQYxQpnhKDN6NufMnDxNpmaRFCj3m3g6pL6wmhEmRfOTk1MDExY2UwOGM4NTQ2Y6FEs6FBAiKaNzUmj8ZRFawj771tqJ8Z+n0eA9i2YYjDyZB/3n6iBom0v62d2gjdirnKXXOtwEQPZIS0Kl6lD09ed0kylkmhEmRfOWU5NDMwMTg1OTczNTRjY6FEs6FBAh43gXn9/qD6d3CMpxmLG9OuD6855tpJR/dL3/lmh3YfF/ot8dDKvUyOQLKS6r9gKnHoDampibAcAK8h0us+KG2hEmRfYTBlYjdlYTJiMmQzMzlmNKFEs6FBAw+fAv0m181qGIbYJOL1bvBqxpv4Q0xidxalFm5qP+TTFEvDGMmeeIWL4qPnTftlk/3gRSCAyrhIRoNZluAuaFihEmRfYTIwMWVmOTE4OGI2ZWJhOaFEs6FBAgKGKRwqSnCue9sEUJRG29TJpjgTlMtK6RYuffqyLQU+ISNjmbqq2kYtHwkg2fvb+H245WIZ7Z04GJLY2w9Ryr6hEmRfYTZhMjZmYjg3ZWRhMzZjMaFEs6FBAgAhoEQNA/GL3Kwt6e4IrIeFYnC6I9sJowVGUS2lB2lRG2XnRbLQehrH1Saf4IfMKaJwKMxcoA10xcWPAGrOK4OhEmRfYjdiZTRlNDNiNDNiNTQ4NaFEs6FBAh27bOZxN+RVjpdI9wL1sLRdEG6oZjdONjdgbmsdahrZBQVjdvNDSsacmDLiYuaXxzFkFvbbAWCgwWZZFv8Qod6hEmRfY2VhY2RkYjg5Y2U5NTA1ZaFEs6FBAwU3Imi6FZcIAL0Laz8n3e2zxex1xTRzflRCcrOq6qiAEUTeNq454wyvcyuc/xdGCq1jAeph0lYCKkgEW0Vor7uhEmRfZDQ0NWE5NjA0OGQwZjg5NaFEs6FBAwiZpY0DQxV5/+cTTxuuz4qBIboOhOYeBesS+s4PwwabHIQpZZqafIDxxsJvQ59Ir12ZLiv4hFhNqWqV6UWel8+hEmRfZGIzN2ViYmI0MjE4ODk1OaFEs6FBAhgjJfg8ooRrjOpJeI1bMwW83vM+NyXoBUrl9lMnTtnSHHjpuoQpfPCaRK0mkfHdgReHPGDF2BGc2N6hTH2vZ4ahEmRfZWQzZWJmOWI0YjI0MTJlMaFEs6FBAwTbdVwglWX+rLEVnHyHniPj4vTXJ786Hwn6rauqKqb6ETLA3OQRpm9ECoUu1nvI/2tdNQtRw9xOx+Ikd4wtmTGhEmRfZjEzNzE0NGFiZDZjMjI2ZaFEs6FBAgoUaS5PxPsEM7t78smmZT3WA5hlA5QHZygUnmjgMuQaFFOie9EZArQ4qHZJaHZGVYUExjFI/3pd/+VKgmlFJpKhBWlucHV0oV0dAAAAWGFkNjJiNzgzMmZjMWY0Zjk0MmE1ZTFjNjE4ZmJlMTAyOTliYTNhYmI3ZGMwYWJlYTczN2YyNTM0NWYwMjBmOTAwIEFORCB0aW1lID49IDE1NDYzMDA4MDA=\"" + 
			"  }                                   " + 
			"}";
		
		var httpClient = 
			HttpClient.newBuilder().build();

		var request = HttpRequest
			.newBuilder(new URI("http://localhost:8080/share"))
			.header("Content-Type", ContentType.json.getContentTypeString())
			.POST(BodyPublishers.ofString(json))
			.build();
		
		var response = httpClient.send(request, BodyHandlers.ofString());
		
		System.out.println(response.statusCode());
		
		System.out.println(response.body());
	}
}
