package org.sunbird.core.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunbird.common.util.CbExtServerProperties;

@Configuration
public class EsConfig {
	@Autowired
	CbExtServerProperties configuration;

	@Bean(name = "esClient", destroyMethod = "close")
	public RestHighLevelClient getCbEsRestClient(CbExtServerProperties configuration) {
		return createRestClient(configuration.getEsHost(), configuration.getEsPort(), configuration.getEsUser(),
				configuration.getEsPassword());
	}

	@Bean(name = "sbEsClient", destroyMethod = "close")
	public RestHighLevelClient getSbESRestClient(CbExtServerProperties configuration) {
		return createRestClient(configuration.getSbEsHost(), configuration.getSbEsPort(), configuration.getSbEsUser(),
				configuration.getSbEsPassword());
	}

	private RestHighLevelClient createRestClient(String host, String port, String user, String password) {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

		RestClientBuilder builder = RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
				.setHttpClientConfigCallback(
						httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

		return new RestHighLevelClient(builder);
	}
}