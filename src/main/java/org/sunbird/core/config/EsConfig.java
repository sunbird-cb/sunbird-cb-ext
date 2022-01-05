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

	@Bean(destroyMethod = "close")
	public RestHighLevelClient restHighLevelClient(CbExtServerProperties configuration) {

		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(configuration.getEsUser(), configuration.getEsPassword()));

		RestClientBuilder builder = RestClient
				.builder(new HttpHost(configuration.getEsHost(), Integer.parseInt(configuration.getEsPort())))
				.setHttpClientConfigCallback(
						httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

		return new RestHighLevelClient(builder);

	}
}