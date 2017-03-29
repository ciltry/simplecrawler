/**
 * 
 */
package dev.sidney.crawler.simplecrawler.domain.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import dev.sidney.crawler.simplecrawler.domain.TaskItemDomain;
import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
import dev.sidney.crawler.simplecrawler.model.TaskItem;
import dev.sidney.devutil.store.dao.CommonDAO;
import dev.sidney.devutil.store.domain.BaseDomain;

/**
 * @author 杨丰光 2017年3月23日15:33:55
 *
 */
@Service
public class TaskItemDomainImpl extends BaseDomain<TaskItemDTO> implements TaskItemDomain{

	@Resource(name="simplecrawlerDao")
	private CommonDAO dao;

	@Override
	protected CommonDAO getDAO() {
		return dao;
	}

	@Override
	public void prepareTaskItem(String taskId) {
		TaskItem taskItem = new TaskItem();
		taskItem.setStatus("I");
		this.dao.update(taskItem, String.format("%s = ? AND %s = ?", TaskItem.COLUMN_TASK_ID, TaskItem.COLUMN_STATUS), taskId, "P");
	}
}
