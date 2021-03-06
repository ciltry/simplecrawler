/**
 * 
 */
package dev.sidney.crawler.simplecrawler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Assert;

import dev.sidney.crawler.simplecrawler.domain.TaskDomain;
import dev.sidney.crawler.simplecrawler.domain.TaskItemDomain;
import dev.sidney.crawler.simplecrawler.dto.TaskDTO;
import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
import dev.sidney.crawler.simplecrawler.enums.TaskItemStatusEnum;
import dev.sidney.crawler.simplecrawler.model.Image;
import dev.sidney.devutil.store.dao.CommonDAO;

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
	
	@Resource(name="simplecrawlerDao")
	private CommonDAO dao;
//	HttpHost proxy = new HttpHost("someproxy", 8080);
//	DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
	private PoolingHttpClientConnectionManager pcm = new PoolingHttpClientConnectionManager();
	private CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(pcm).setDefaultRequestConfig(RequestConfig.custom()
	        .setCookieSpec(CookieSpecs.STANDARD)
	        .build())
	        //.setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost("127.0.0.1", 8888)))
	        .build();
	
	
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
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(4){
			@Override
			public boolean offer(Runnable arg0) {
				try {
					this.put(arg0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			}};
		executorService = new ThreadPoolExecutor(this.getMaxThreads(), this.getMaxThreads(), 1, TimeUnit.HOURS, queue, new ThreadPoolExecutor.DiscardPolicy());
		new Thread(this).start();
		return this.getTaskId();
	}
	
	private TaskDTO loadTask() {
		TaskDTO task = new TaskDTO();
		task.setTaskName(this.getTaskName());
		task = this.taskDomain.query(task);
		if (task != null) {
			this.taskItemDomain.prepareTaskItem(task.getId());
		}
		return task;
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
		taskItem.setType("HTML");
		this.taskItemDomain.insert(taskItem);
	}
	@Override
	public void run() {
			while (true) {
				TaskItemDTO taskItem = this.peekStandbyTaskItem();
				if (taskItem == null) {
					if (this.taskDomain.isTaskFinished(this.getTaskId())) {
						break;
					} else {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
			System.out.println(String.format("任务结束: %s", this.getTaskName()));
	}
	
	private TaskItemDTO peekStandbyTaskItem() {
		TaskItemDTO taskItem = new TaskItemDTO();
		taskItem.setTaskId(this.getTaskId());
		taskItem.setStatus(TaskItemStatusEnum.INITIAL.getCode());
		return this.taskItemDomain.peek(taskItem);
	}
	@Override
	public final void process(TaskItemDTO taskItem, String pageContent) {
		if ("HTML".equals(taskItem.getType())) {
			
			this.processPage(taskItem, pageContent);
			URL url = null;
			try {
				url = new URL(taskItem.getUrl());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<String> urlList = scanUrl(url, pageContent);
			saveTaskItem(taskItem, urlList, "HTML");
			if (this.allowImage()) {
				List<String> imgList = scanImgUrl(url, pageContent);
				for (String imgUrl: imgList) {
					if (this.allowImage(imgUrl)) {
						this.saveTaskItem(taskItem, imgUrl, "IMG");
					}
				}
			}
		}
		this.taskItemFinished(taskItem);
	}
	
	private void saveTaskItem(TaskItemDTO parentTaskItem, List<String> urlList, String taskType) {
		for (String url: urlList) {
			this.saveTaskItem(parentTaskItem, url, taskType);
		}
	}
	private void saveTaskItem(TaskItemDTO parentTaskItem, String url, String taskType) {
		if (!this.skipUrl(url) && !isUrlExists(url)) {
			TaskItemDTO taskItem = new TaskItemDTO();
			taskItem.setUrl(url);
			taskItem.setTaskId(this.getTaskId());
			taskItem.setStatus(TaskItemStatusEnum.INITIAL.getCode());
			taskItem.setParentTaskItem(parentTaskItem.getId());
			taskItem.setType(taskType);
			this.taskItemDomain.insert(taskItem);
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
	
	private String getStackTrace(Throwable t) {  
	    StringWriter sw = new StringWriter();  
	    PrintWriter pw = new PrintWriter(sw);  
	  
	    try {
	        t.printStackTrace(pw);  
	        return sw.toString();  
	    }  
	    finally {
	        pw.close();  
	    }  
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
	
	private List<String> scanImgUrl(URL pageUrl, String pageContent) {
		List<String> list = new ArrayList<String>();
		Pattern pt = Pattern.compile("<\\s*img\\s+[^>]*src=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
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
		
//		synchronized (this.getTaskId().intern()) {
			TaskItemDTO updateDto = new TaskItemDTO();
			updateDto.setId(taskItemDTO.getId());
			updateDto.setStatus(TaskItemStatusEnum.SUCCESS.getCode());
			this.taskItemDomain.updateById(updateDto);
//			this.getTaskId().intern().notify();
//		}
	}
	
	private void updateToProcessing(TaskItemDTO taskItemDTO) {
		TaskItemDTO updateDto = new TaskItemDTO();
		updateDto.setId(taskItemDTO.getId());
		updateDto.setStatus(TaskItemStatusEnum.PROCESSING.getCode());
		this.taskItemDomain.updateById(updateDto);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
//		List<Integer> l1 = new ArrayList<Integer>();
//        for (int i = 1; i <= 9; i++) {
//        	for (int j = 1; j <= 9; j++) {
//        		if (j == i) {
//        			continue;
//        		}
//        		l1.add(i * 10 + j);
//        	}
//        }
//        System.out.println(Arrays.deepToString(l1.toArray()));
//        for (int i = 0; i < l1.size(); i++) {
//        	for (int j = 0; j < l1.size(); j++) {
//        		if (i == j) {
//        			continue;
//        		}
//        		Integer numerator = l1.get(j);
//        		Integer denominator = l1.get(i);
//        		if (numerator > denominator) {
//        			if ((numerator % 10) == (denominator / 10) && ((double) numerator / denominator) == ((double) (numerator / 10) / (denominator % 10))) {
//        				System.out.println("1: " + denominator + "/" + numerator);
//        			} else if ((numerator / 10) == (denominator % 10) && ((double) numerator / denominator) == ((double) (numerator % 10) / (denominator / 10))) {
//        				System.out.println("2: " + denominator + "/" + numerator);
//        			} else if ((numerator % 10) == (denominator % 10) && ((double) numerator / denominator) == ((double) (numerator / 10) / (denominator / 10))) {
//        				System.out.println("3: " + denominator + "/" + numerator);
//        			}
//        		}
//        	}
//        }
		
		long currentMax = 0;
		int x = 0;
		int n = 0;
		
		for (int i = 1; i <= 9999; i++) {
			long total = 0;
			long lastTotal = 0;
			String totalStr = "";
			int jOfThisRound = 0;
			for (int j = 1; total < 1000000000;j++) {
				totalStr += (i * j);
				lastTotal = total;
				total = Long.parseLong(totalStr);
				jOfThisRound = j;
			}
			System.out.println(i + " " + (jOfThisRound - 1));
			if (lastTotal > currentMax) {
				x = i;
				n = jOfThisRound - 1;
				currentMax = lastTotal;
			}
		}
		System.out.println(String.format("%d and (1,2,...,%d)  max:%d", x, n, currentMax));
    }
	@Override
	public final void handleException(TaskItemDTO taskItem, Exception e) {
		TaskItemDTO update = new TaskItemDTO();
		update.setId(taskItem.getId());
		update.setStatus("E");
		update.setExceptionTrace(getStackTrace(e));
		this.taskItemDomain.updateById(update);
	}
	
	
	@Override
	public boolean isImageExists(String url) {
		Image img = new  Image();
		img.setUrl(url);
		img.setTaskId(this.getTaskId());
		return this.dao.queryForObject(img) != null;
	}
	
	protected boolean allowImage() {
		return false;
	}
	
	protected boolean allowImage(String url) {
		return true;
	}
	
	protected String getFileNameFromUrl(String url) {
		if (url.indexOf("?") >= 0) {
			url = url.substring(0, url.lastIndexOf("?"));
		}
		String fileName = url;
		if (url.indexOf("/") >= 0) {
			fileName = url.substring(url.lastIndexOf("/") + 1);
		}
		return fileName;
	}
	@Override
	public final void processImageData(TaskItemDTO taskItem, byte[] data) {
		this.processImage(taskItem.getUrl(), data);
		this.taskItemFinished(taskItem);
	}
	
	protected void processImage(String url, byte[] data) {
		
	}
}
