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
		List<String> hostList = null;
		if (!StringUtils.isEmpty(host) && !StringUtils.isEmpty(port)) {
			String[] splitHost = host.split(",");
			logger.info("splitHost : "+splitHost.toString());
			for (String val : splitHost) {
                logger.info("IPS are : "+val);
				hostList.add(val);
			}
		}
		HttpHost[] httpHost = new HttpHost[hostList.size()];
		for (int i = 0; i < hostList.size(); i++) {
			httpHost[i] = new HttpHost(hostList.get(i), Integer.parseInt(port));
		}
		logger.info("httpHost is : "+httpHost.toString());
		RestClientBuilder builder = RestClient.builder(httpHost)
				.setHttpClientConfigCallback(
						httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

		return new RestHighLevelClient(builder);
	}
}