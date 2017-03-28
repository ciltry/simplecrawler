/**
 * 
 */
package dev.sidney.crawler.simplecrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dev.sidney.crawler.simplecrawler.domain.TaskItemDomain;
import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
import dev.sidney.crawler.simplecrawler.model.Task;
import dev.sidney.crawler.simplecrawler.model.TaskItem;
import dev.sidney.devutil.store.dao.CommonDAO;
import dev.sidney.devutil.store.domain.DomainQuery;
import dev.sidney.proxypool.domain.ServerDomain;
import dev.sidney.proxypool.dto.ServerDTO;
import dev.sidney.proxypool.model.Server;

/**
 * @author 杨丰光 2017年3月23日15:00:46
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/context/*.xml" })
public class ModelTest {

	@Resource(name="simplecrawlerDao")
	private CommonDAO dao;
	
	@Resource(name="proxyDao")
	private CommonDAO proxyDdao;
	
	@Resource
	private TaskItemDomain taskItemDomain;
	
	@Resource(name="kuaidaili")
	private ICrawlerTask task;
	@Resource
	private ServerDomain serverDomain;
	
	@Test
	public void init() {
		proxyDdao.dropStore(Server.class);
		dao.dropStore(Task.class);
		dao.dropStore(TaskItem.class);
		proxyDdao.init();
		dao.init();
	}
	
	@Test
	public void testQueryForList() {
		
		DomainQuery query = new DomainQuery().equals(TaskItem.COLUMN_STATUS, "P").and(new DomainQuery().equals(TaskItem.COLUMN_URL, "http://www.kuaidaili.com/free/inha/1107/").or(new DomainQuery().equals(TaskItem.COLUMN_URL, "http://www.kuaidaili.com/free/inha/434/")));
		List<TaskItem> list = dao.queryForList(TaskItem.class, query);
		
		System.out.println(list.size());
	}
	
	@Test
	public void test1() {
		
//		dao.dropStore(TaskItem.class);
		dao.init();
		
		
		TaskItem i = new TaskItem();
		i.setParentTaskItem("243626");
		i.setStatus("A");
		i.setTaskId("623424");
		i.setUrl("http://www.kuaidaili.com/free/outha/1/");
		dao.insert(i);
		
		TaskItem a = new TaskItem();
		a.setId(i.getId());
		a = dao.queryForObject(a);
		System.out.println(a);
	}
	@Test
	public void test2() {
		TaskItemDTO i = new TaskItemDTO();
		i.setParentTaskItem("243626");
		i.setStatus("A");
		i.setTaskId("623424");
		i.setUrl("http://www.kuaidaili.com/free/outha/1/");
		i.setExceptionTrace("flawkjelf");
		
		taskItemDomain.insert(i);
		
		TaskItemDTO dto = taskItemDomain.queryById(i.getId());
		
		System.out.println(dto);
	}
	
	@Test
	public void test3() {
		TaskItemDTO query = new TaskItemDTO();
		query.setExceptionTrace("flawkjelf");
		TaskItemDTO res = taskItemDomain.peek(query);
		System.out.println(res);
	}
	
	
	@Test
	public void test5() throws IOException {
		URL u = new URL("http://poss-test.masapay.com/poss-web/login.jsp?sign=You%20are%20not%20login");
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.getInputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
		StringBuilder res = new StringBuilder();
		while (true) {
			String str = reader.readLine();
			if (str == null) {
				break;
			}
			res.append(str);
		}
		reader.close();
		System.out.println(res.toString());
	}
	
	
	@Test
	public void test4() {
//		ICrawlerTask task = new CrawlerKuaiDaiLi();
		task.setMaxThreads(1);
		task.setStartUrl("http://www.kuaidaili.com/");
//		task.setStartUrl("http://www.baidu.com/");
//		task.setStartUrl("http://ifeve.com/overview/");
//		task.setStartUrl("http://poss-test.masapay.com/poss-web/login.jsp?sign=You%20are%20not%20login");
		task.setTaskName("快代理");
		task.start();
		
		try {
			Thread.sleep(50000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testServer() {
		ServerDTO query = new ServerDTO();
		query.setStatus("0");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date(new Date().getTime() - 60000*60);
		System.out.println(sdf.format(d));
		System.out.println(sdf.format(new Date()));
		while (true) {
			Server server = proxyDdao.queryForObject("SELECT * FROM (select ROW_NUMBER() OVER() AS R, t.* from APP.SERVER T WHERE (T.STATUS IS NULL OR T.STATUS != '1') AND (T.GMT_LAST_TEST IS NULL OR T.GMT_LAST_TEST < ?)  order by t.gmt_create) T WHERE T.R=1", new Object[]{d}, new RowMapper<Server>(){
				@Override
				public Server mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					Server server = new Server();
					server.setId(rs.getString("ID"));
					server.setIp(rs.getString("IP"));
					server.setPort(rs.getInt("PORT"));
					server.setLocation(rs.getString("LOCATION"));
					return server;
				}});
			if (server == null) {
				break;
			}
			System.out.println(server.getIp() + " " + server.getLocation());
			
			CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(new PoolingHttpClientConnectionManager()).setDefaultRequestConfig(RequestConfig.custom()
			        .setCookieSpec(CookieSpecs.STANDARD)
			        .build()).setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(server.getIp(), server.getPort()))).build();
			
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
			HttpGet httpget = new HttpGet("http://www.baidu.com");
			httpget.setConfig(requestConfig);
			boolean available = false;
			int speed = 0;
			try {
				Date date = new Date();
				CloseableHttpResponse response =  httpClient.execute(httpget);
				available = response.getStatusLine().getStatusCode() == 200;
				speed = (int) (new Date().getTime() - date.getTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(String.format("%s %dms %s %s", available ? "1" : "0", speed, server.getIp(), server.getLocation()));
			Server update = new Server();
			update.setId(server.getId());
			update.setStatus(available ? "1" : "0");
			update.setGmtLastTest(new Date());
			update.setSpeed(speed);
			this.proxyDdao.update(update);
		}
	}
}
