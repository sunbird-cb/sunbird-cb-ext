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
import org.springframework.util.StringUtils;
import org.sunbird.core.logger.CbExtLogger;

import java.util.List;


@Configuration
public class EsConfig {
	private CbExtLogger logger = new CbExtLogger(getClass().getName());
	@Autowired
	CbExtServerProperties configuration;

	@Bean(name = "esClient", destroyMethod = "close")
	public RestHighLevelClient getCbEsRestClient(CbExtServerProperties configuration) {
		return createRestClient(configuration.getEsHost(), configuration.getEsUser(),
				configuration.getEsPassword());
	}

	@Bean(name = "sbEsClient", destroyMethod = "close")
	public RestHighLevelClient getSbESRestClient(CbExtServerProperties configuration) {
		return createRestClient(configuration.getSbEsHost(), configuration.getSbEsUser(),
				configuration.getSbEsPassword());
	}

	private RestHighLevelClient createRestClient(String[] hosts, String user, String password) {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

		HttpHost[] httpHosts = new HttpHost[hosts.length];
		for (int i = 0; i < httpHosts.length; i++) {
			String hostIp = hosts[i].split(":")[0];
			String hostPort = hosts[i].split(":")[1];
			httpHosts[i] = new HttpHost(hostIp, Integer.parseInt(hostPort));
		}

		RestClientBuilder builder = RestClient.builder(httpHosts).setHttpClientConfigCallback(
				httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

		return new RestHighLevelClient(builder);
	}
}