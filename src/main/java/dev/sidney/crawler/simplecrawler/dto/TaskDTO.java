/**
 * 
 */
package dev.sidney.crawler.simplecrawler.dto;

import dev.sidney.crawler.simplecrawler.model.Task;
import dev.sidney.devutil.store.dto.BaseDTO;

/**
 * @author 杨丰光 2017年3月23日16:43:45
 *
 */
public class TaskDTO extends BaseDTO<Task> {

	
	private String taskName;
	private String startUrl;
	
	@Override
	public Task toModel() {
		Task model = new Task();
		TaskDTO dto = this;
		
		model.setGmtCreate(dto.getGmtCreate());
		model.setGmtModified(dto.getGmtModified());
		model.setId(dto.getId());
		model.setStartUrl(dto.getStartUrl());
		model.setTaskName(dto.getTaskName());
		return model;
	}

	@Override
	public void constructDTO(Task model) {
		TaskDTO dto = this;
		if (model != null) {
			dto.setGmtCreate(model.getGmtCreate());
			dto.setGmtModified(model.getGmtModified());
			dto.setId(model.getId());
			dto.setStartUrl(model.getStartUrl());
			dto.setTaskName(model.getTaskName());
		}
	}
	
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}


}
