/**
 * 
 */
package dev.sidney.crawler.simplecrawler.domain.impl;

import javax.annotation.Resource;

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

}
