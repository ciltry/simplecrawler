/**
 * 
 */
package dev.sidney.crawler.simplecrawler.crawler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
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
		httpGet.setHeader("Cookie", "_ga=GA1.2.208353969.1490336819; Hm_lvt_7ed65b1cc4b810e9fd37959c9bb51b31=1490327374,1490336051,1490336240,1490751870; channelid=0; sid=1490773659460788; Hm_lpvt_7ed65b1cc4b810e9fd37959c9bb51b31=1490774160; _ydclearance=619923b471634c5257141523-08aa-4fad-b449-2fa0698b9e1c-1490781354; _gat=1");
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

	@Override
	public void processImage(String url, byte[] data) {
		try {
			FileUtils.writeByteArrayToFile(new File("img/" + this.getFileNameFromUrl(url)), data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
