/**
 * 
 */
package dev.sidney.crawler.simplecrawler.crawler;

import java.util.List;

import javax.annotation.Resource;

import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Service;

import dev.sidney.crawler.simplecrawler.AbstractCrawlerTask;
import dev.sidney.crawler.simplecrawler.datacapture.DataCapture;
import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
import dev.sidney.proxypool.domain.ServerDomain;
import dev.sidney.proxypool.dto.ServerDTO;

/**
 * @author Sidney
 *
 */
@Service("kuaidaili")
public class CrawlerKuaiDaiLi extends AbstractCrawlerTask {

	@Resource(name="KuaiDaiLiDataCapture")
	private DataCapture<ServerDTO> dataCapture;
	
	@Resource
	private ServerDomain serverDomain;
	
	@Override
	public void initHttpGet(HttpGet httpGet) {
		httpGet.setHeader("Cookie", "channelid=0; sid=1490431227077912; _ydclearance=c01e8fe95232bef2055be613-a7e2-49d2-98fe-1a9f84c32938-1490440999; Hm_lvt_7ed65b1cc4b810e9fd37959c9bb51b31=1490424882,1490431479,1490431501; Hm_lpvt_7ed65b1cc4b810e9fd37959c9bb51b31=1490433808; _ga=GA1.2.1898919598.1490424882; _gat=1");
	}

	@Override
	public boolean skipUrl(String url) {
		return !url.contains("/proxylist/") && !url.contains("/free/") || url.contains("tel:");
	}

	@Override
	protected void processPage(TaskItemDTO taskItem, String pageContent) {
		if (taskItem.getUrl().contains("/free/")) {
			List<ServerDTO> serverList = this.dataCapture.captureData(pageContent);
			saveServerList(serverList);
		}
	}

	private void saveServerList(List<ServerDTO> serverList) {
		for (ServerDTO server: serverList) {
			if (!this.isServerExists(server)) {
				this.serverDomain.insert(server);
			}
		}
	}
	
	private boolean isServerExists(ServerDTO server) {
		ServerDTO query = new ServerDTO();
		query.setIp(server.getIp());
		query.setPort(server.getPort());
		return this.serverDomain.query(query) != null;
	}
}
