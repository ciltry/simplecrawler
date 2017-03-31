package dev.sidney.crawler.simplecrawler;

import org.apache.http.client.methods.HttpGet;

import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
import dev.sidney.crawler.simplecrawler.exceptions.BusinessException;

public interface ICrawlerTask {

	/**
	 * 启动爬虫
	 * @return taskid
	 */
	String start();


	/**
	 * 设置taskId
	 * @param taskId
	 */
	void setTaskId(String taskId);


	void setStartUrl(String startUrl);


	void setTaskName(String taskName);


	void setMaxThreads(int maxThreads);
	
	void initHttpGet(HttpGet httpGet);
	
	void process(TaskItemDTO taskItem, String pageContent);
	void processImageData(TaskItemDTO taskItem, byte[] data);
	
	boolean skipUrl(String url);
	
	void validate(TaskItemDTO taskItem, String pageContent) throws BusinessException;
	void handleException(TaskItemDTO taskItem, Exception e);
	boolean isImageExists(String url);
}