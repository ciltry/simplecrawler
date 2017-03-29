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
import dev.sidney.crawler.simplecrawler.exceptions.BusinessException;
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
		httpGet.setHeader("Cookie", "_ga=GA1.2.208353969.1490336819; Hm_lvt_7ed65b1cc4b810e9fd37959c9bb51b31=1490327374,1490336051,1490336240,1490751870; channelid=0; sid=1490755207720413; Hm_lpvt_7ed65b1cc4b810e9fd37959c9bb51b31=1490756207; _ydclearance=b2f63576e1cb8a3392db1687-0326-485a-9705-99dfa981cd77-1490773935");
	}

	@Override
	public boolean skipUrl(String url) {
		return !url.contains("/proxylist/") && !url.contains("/free/") || url.contains("tel:");
	}

	@Override
	protected void processPage(TaskItemDTO taskItem, String pageContent) {
		if (taskItem.getUrl().contains("/free/")) {
			List<ServerDTO> serverList = this.dataCapture.captureData(taskItem, pageContent);
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

	@Override
	public void validate(TaskItemDTO taskItem, String pageContent) throws BusinessException {
		if (!pageContent.contains("title")) {
			throw new BusinessException("cookie不对");
		}
	}
}
