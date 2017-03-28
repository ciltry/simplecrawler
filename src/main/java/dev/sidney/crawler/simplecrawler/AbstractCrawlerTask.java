/**
 * 
 */
package dev.sidney.crawler.simplecrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Assert;

import dev.sidney.crawler.simplecrawler.domain.TaskDomain;
import dev.sidney.crawler.simplecrawler.domain.TaskItemDomain;
import dev.sidney.crawler.simplecrawler.dto.TaskDTO;
import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
import dev.sidney.crawler.simplecrawler.enums.TaskItemStatusEnum;

/**
 * @author 杨丰光 2017年3月23日14:43:46
 *
 */
public abstract class AbstractCrawlerTask implements ICrawlerTask, Runnable {
	
	/**
	 * 任务名
	 */
	private String taskName;
	
	/**
	 * 入口
	 */
	private String startUrl;
	
	private String taskId;
	
	/**
	 * 最大线程数
	 */
	private int maxThreads;
	
	@Resource
	private TaskItemDomain taskItemDomain;
	
	@Resource
	private TaskDomain taskDomain;
	
	private ExecutorService executorService;
//	HttpHost proxy = new HttpHost("someproxy", 8080);
//	DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	private PoolingHttpClientConnectionManager pcm = new PoolingHttpClientConnectionManager();
	private CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(pcm).setDefaultRequestConfig(RequestConfig.custom()
	        .setCookieSpec(CookieSpecs.STANDARD)
	        .build()).setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost("127.0.0.1", 8888))).build();
	
	
	@Override
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}
	public int getMaxThreads() {
		return maxThreads;
	}

	public String getTaskName() {
		return taskName;
	}

	@Override
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskId() {
		return taskId;
	}

	
	public String getStartUrl() {
		return startUrl;
	}

	@Override
	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	/* (non-Javadoc)
	 * @see dev.sidney.crawler.simplecrawler.ICrawlerTask#start()
	 */
	@Override
	public final String start() {
		if (StringUtils.isBlank(this.getTaskId())) {
			
			TaskDTO task = this.loadTask();
			if (task != null) {
				this.setTaskId(task.getId());
			} else {
				Assert.assertNotNull(this.getStartUrl());
				createTask();
				createTaskItem(this.getStartUrl());
			}
			
		}
//		executorService = Executors.newFixedThreadPool(this.getMaxThreads() + 1);
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(3);
		executorService = new ThreadPoolExecutor(4, 4, 1, TimeUnit.HOURS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
		executorService.execute(this);
		return this.getTaskId();
	}
	
	private TaskDTO loadTask() {
		TaskDTO task = new TaskDTO();
		task.setTaskName(this.getTaskName());
		return this.taskDomain.query(task);
	}
	
	private String normalizeUrl(String url) {
		if (url.lastIndexOf("#") > 0) {
			url = url.substring(0, url.lastIndexOf("#"));
		}
		return url;
	}
	
	private void createTask() {
		TaskDTO taskDTO = new TaskDTO();
		taskDTO.setStartUrl(startUrl);
		taskDTO.setTaskName(taskName);
		this.taskDomain.insert(taskDTO);
		this.setTaskId(taskDTO.getId());
	}
	
	private void createTaskItem(String url) {
		url = normalizeUrl(url);
		TaskItemDTO taskItem = new TaskItemDTO();
		
		taskItem.setUrl(url);
		taskItem.setTaskId(this.getTaskId());
		taskItem.setStatus(TaskItemStatusEnum.INITIAL.getCode());
		this.taskItemDomain.insert(taskItem);
	}
	@Override
	public void run() {
		synchronized (this.getTaskId().intern()) {
			while (true) {
				TaskItemDTO taskItem = this.peekStandbyTaskItem();
				if (taskItem == null) {
					try {
						this.getTaskId().intern().wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					updateToProcessing(taskItem);
					CrawlerWorker worker = new CrawlerWorker();
					worker.setTaskItem(taskItem);
					worker.setHttpClient(this.httpClient);
					worker.setTask(this);
					this.executorService.execute(worker);
				}
			}
		}
	}
	
	private TaskItemDTO peekStandbyTaskItem() {
		TaskItemDTO taskItem = new TaskItemDTO();
		taskItem.setTaskId(this.getTaskId());
		taskItem.setStatus(TaskItemStatusEnum.INITIAL.getCode());
		return this.taskItemDomain.peek(taskItem);
	}
	@Override
	public final void process(TaskItemDTO taskItem, String pageContent) {
		this.processPage(taskItem, pageContent);
		URL url = null;
		try {
			url = new URL(taskItem.getUrl());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> urlList = scanUrl(url, pageContent);
		saveTaskItem(taskItem, urlList);
		this.taskItemFinished(taskItem);
	}
	
	private void saveTaskItem(TaskItemDTO parentTaskItem, List<String> urlList) {
		for (String url: urlList) {
			if (!this.skipUrl(url) && !isUrlExists(url)) {
				TaskItemDTO taskItem = new TaskItemDTO();
				taskItem.setUrl(url);
				taskItem.setTaskId(this.getTaskId());
				taskItem.setStatus(TaskItemStatusEnum.INITIAL.getCode());
				taskItem.setParentTaskItem(parentTaskItem.getId());
				this.taskItemDomain.insert(taskItem);
			}
		}
	}
	
	private boolean isUrlExists(String url) {
		boolean exists = false;
		TaskItemDTO query = new TaskItemDTO();
		query.setUrl(url);
		query.setTaskId(this.getTaskId());
		exists = this.taskItemDomain.query(query) != null;
		return exists;
	}
	
	protected abstract void processPage(TaskItemDTO taskItem, String pageContent);
	
	private boolean isFullUrl(String url) {
		boolean isFullUrl = url.toLowerCase().startsWith("http:") || url.toLowerCase().startsWith("https") || url.toLowerCase().startsWith("ftp:");
		return isFullUrl;
	}
	
	private boolean isLegalUrl(String url) {
		return !url.toLowerCase().contains("javascript:");
	}
	
	private List<String> scanUrl(URL pageUrl, String pageContent) {
		List<String> list = new ArrayList<String>();
		Pattern pt = Pattern.compile("<\\s*a\\s+[^>]*href=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
		String protocol = pageUrl.getProtocol();
		String host = pageUrl.getHost();
		Matcher matcher = pt.matcher(pageContent);
		while (matcher.find()) {
			String url = matcher.group(1);
			if (!isFullUrl(url)) {
				if (url.startsWith("/")) {
					url = String.format("%s://%s%s", protocol, host, url);
				} else {
					url = pageUrl.toString().substring(0, pageUrl.toString().lastIndexOf("/") + 1) + url;
				}
			}
			url = this.normalizeUrl(url);
			if (!list.contains(url) && isLegalUrl(url)) {
				list.add(url);
			}
		}
		return list;
	}
	
	
	
	
	private void taskItemFinished(TaskItemDTO taskItemDTO) {
		
		synchronized (this.getTaskId().intern()) {
			TaskItemDTO updateDto = new TaskItemDTO();
			updateDto.setId(taskItemDTO.getId());
			updateDto.setStatus(TaskItemStatusEnum.SUCCESS.getCode());
			this.taskItemDomain.updateById(updateDto);
			this.getTaskId().intern().notify();
		}
	}
	
	private void updateToProcessing(TaskItemDTO taskItemDTO) {
		TaskItemDTO updateDto = new TaskItemDTO();
		updateDto.setId(taskItemDTO.getId());
		updateDto.setStatus(TaskItemStatusEnum.PROCESSING.getCode());
		this.taskItemDomain.updateById(updateDto);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {  
        
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(0);  
          
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.HOURS, queue, new ThreadPoolExecutor.CallerRunsPolicy());  
          
        for (int i = 0; i < 10; i++) {  
            final int index = i;  
            System.out.println("task: " + (index+1));  
            Runnable run = new Runnable() {  
                @Override  
                public void run() {  
                    System.out.println("thread start" + (index+1));  
                    try {  
                        Thread.sleep(Long.MAX_VALUE);  
                    } catch (InterruptedException e) {  
                        e.printStackTrace();  
                    }  
                    System.out.println("thread end" + (index+1));  
                }  
            };  
            executor.execute(run);  
        }  
    }  
}
