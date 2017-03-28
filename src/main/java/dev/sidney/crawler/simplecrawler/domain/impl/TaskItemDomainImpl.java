/**
 * 
 */
package dev.sidney.crawler.simplecrawler.domain.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import dev.sidney.crawler.simplecrawler.domain.TaskItemDomain;
import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;
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

}
