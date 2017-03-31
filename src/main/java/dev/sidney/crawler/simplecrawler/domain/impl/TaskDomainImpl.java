/**
 * 
 */
package dev.sidney.crawler.simplecrawler.domain.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Resource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import dev.sidney.crawler.simplecrawler.domain.TaskDomain;
import dev.sidney.crawler.simplecrawler.dto.TaskDTO;
import dev.sidney.devutil.store.dao.CommonDAO;
import dev.sidney.devutil.store.domain.BaseDomain;

/**
 * @author 杨丰光 2017年3月23日16:48:49
 *
 */
@Service
public class TaskDomainImpl extends BaseDomain<TaskDTO> implements TaskDomain {

	@Resource(name="simplecrawlerDao")
	private CommonDAO dao;

	@Override
	protected CommonDAO getDAO() {
		return dao;
	}

	@Override
	public boolean isTaskFinished(String taskId) {
		Integer unfinishedCount = dao.queryForObject("SELECT COUNT(1) CNT FROM CRAWLER.TASK_ITEM T WHERE T.STATUS IN ('I', 'P') AND T.TASK_ID=?", new Object[]{taskId}, new RowMapper<Integer>(){
			@Override
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getInt("CNT");
			}});
		return unfinishedCount == 0;
	}

}
