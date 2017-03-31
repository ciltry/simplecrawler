/**
 * 
 */
package dev.sidney.crawler.simplecrawler.crawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Service;

import dev.sidney.crawler.simplecrawler.AbstractCrawlerTask;
import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
import dev.sidney.crawler.simplecrawler.exceptions.BusinessException;

/**
 * @author Sidney
 *
 */
@Service("imgTask")
public class CrawlerImg extends AbstractCrawlerTask {

	@Override
	public void initHttpGet(HttpGet httpGet) {
		RequestConfig requestConfig = RequestConfig.custom()  
		        .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
		        .setSocketTimeout(5000).build(); 
		httpGet.setConfig(requestConfig);
	}

	@Override
	protected void processImage(String url, byte[] data) {
		try {
			FileUtils.writeByteArrayToFile(new File("bleach/" + this.getFileNameFromUrl(url)), data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean skipUrl(String url) {
		if (url.contains("kw=死神") || url.matches("https://tieba.baidu.com/p/\\d+(\\?pn=\\d+)?") || url.matches("http://tieba.baidu.com/p/\\d+(\\?pn=\\d+)?")
				|| url.contains("/forum/w%3D580")) {
			return false;
		}
		return true;
	}

	@Override
	public void validate(TaskItemDTO taskItem, String pageContent)
			throws BusinessException {
		if (!pageContent.contains("死神吧")) {
			throw new BusinessException("不在吧内");
		}
	}

	@Override
	protected void processPage(TaskItemDTO taskItem, String pageContent) {
		if (taskItem.getParentTaskItem() == null) {
			try {
				FileUtils.write(new File("主页.html"), pageContent);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean allowImage() {
		return true;
	}

	@Override
	protected boolean allowImage(String url) {
		return true;
	}

	
}
