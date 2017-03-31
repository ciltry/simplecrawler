/**
 * 
 */
package dev.sidney.crawler.simplecrawler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;

/**
 * @author 杨丰光 2017年3月23日14:44:50
 *
 */
public class CrawlerWorker implements Runnable {

	private TaskItemDTO taskItem;
	
	private ICrawlerTask task;
	
	private CloseableHttpClient httpClient;
	
	
	
	public ICrawlerTask getTask() {
		return task;
	}



	public void setTask(ICrawlerTask task) {
		this.task = task;
	}



	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}



	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}



	public TaskItemDTO getTaskItem() {
		return taskItem;
	}



	public void setTaskItem(TaskItemDTO taskItem) {
		this.taskItem = taskItem;
	}


	@Override
	public void run() {
		String content = null;
		try {
			System.out.println("****" + Thread.currentThread().getName() +" : " + this.getTaskItem().getUrl());
			if ("HTML".equals(this.getTaskItem().getType())) {
				content = this.getPageContent();
				this.getTask().validate(this.getTaskItem(), content);
				this.getTask().process(this.getTaskItem(), content);
			} else if ("IMG".equals(this.getTaskItem().getType())) {
				byte[] imgData = this.getImage();
				this.getTask().processImageData(this.getTaskItem(), imgData);
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.getTask().handleException(this.getTaskItem(), e);
			if (content != null) {
				try {
					FileUtils.write(new File(this.getTaskItem().getId() + ".txt"), content);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	
	private String getPageContent(HttpEntity entity) throws ParseException, IOException  {
		String str = EntityUtils.toString(entity);
		return str;
	}
	
	private byte[] getImage() throws IOException {
		HttpGet httpget = new HttpGet(this.getTaskItem().getUrl());
		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
//		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.22 Safari/537.36 SE 2.X MetaSr 1.0");
//		httpget.setHeader("Accept-Encoding", "identity");
		httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//		httpget.setHeader("Accept-Encoding", "gzip, deflate");
		httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		httpget.setHeader("Host", "www.kuaidaili.com");
//		httpget.setHeader("Connection", "keep-alive");
		

		
		task.initHttpGet(httpget);
//		System.out.println(httpget.getURI().toString());
		CloseableHttpResponse response = this.getHttpClient().execute(httpget, HttpClientContext.create());
		HttpEntity entity = null;
		byte[] data = null;
		try {
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				entity = response.getEntity();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buffer = new byte[2048];
				InputStream in = entity.getContent();
				while (true) {
					int ret = in.read(buffer);
					if (ret >= 0) {
						bos.write(buffer, 0, ret);
					} else {
						break;
					}
				}
				data = bos.toByteArray();
				in.close();
			}
		} finally {
			response.close();
		}
		return data;
	}
	
	private String getPageContent() throws ClientProtocolException, IOException {
		HttpGet httpget = new HttpGet(this.getTaskItem().getUrl());
		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
//		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.22 Safari/537.36 SE 2.X MetaSr 1.0");
//		httpget.setHeader("Accept-Encoding", "identity");
		httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//		httpget.setHeader("Accept-Encoding", "gzip, deflate");
		httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		httpget.setHeader("Host", "www.kuaidaili.com");
//		httpget.setHeader("Connection", "keep-alive");
		

		
		task.initHttpGet(httpget);
//		System.out.println(httpget.getURI().toString());
		CloseableHttpResponse response = this.getHttpClient().execute(httpget, HttpClientContext.create());
		HttpEntity entity = null;
		String content = null;
		try {
			entity = response.getEntity();
			content = EntityUtils.toString(entity);
		} finally {
			response.close();
		}
		return content;
	}

}
