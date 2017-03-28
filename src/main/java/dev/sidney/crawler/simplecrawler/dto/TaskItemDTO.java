/**
 * 
 */
package dev.sidney.crawler.simplecrawler.dto;

import dev.sidney.crawler.simplecrawler.model.TaskItem;
import dev.sidney.devutil.store.dto.BaseDTO;

/**
 * @author 杨丰光 2017年3月23日15:14:57
 *
 */
public class TaskItemDTO extends BaseDTO<TaskItem> {

	public TaskItemDTO() {
		
	}
	public TaskItemDTO(TaskItem model) {
		super(model);
	}
	private String taskId;
	private String parentTaskItem;
	private String url;
	private String status;
	private String exceptionTrace;
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getParentTaskItem() {
		return parentTaskItem;
	}
	public void setParentTaskItem(String parentTaskItem) {
		this.parentTaskItem = parentTaskItem;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getExceptionTrace() {
		return exceptionTrace;
	}
	public void setExceptionTrace(String exceptionTrace) {
		this.exceptionTrace = exceptionTrace;
	}
	@Override
	public TaskItem toModel() {
		
		TaskItemDTO dto = this;
		TaskItem model = new TaskItem();
		model.setExceptionTrace(dto.getExceptionTrace());
		model.setGmtCreate(dto.getGmtCreate());
		model.setGmtModified(dto.getGmtModified());
		model.setId(dto.getId());
		model.setParentTaskItem(dto.getParentTaskItem());
		model.setStatus(dto.getStatus());
		model.setTaskId(dto.getTaskId());
		model.setUrl(dto.getUrl());
		return model;
	}
	@Override
	public void constructDTO(TaskItem model) {
		TaskItemDTO dto = this;
		if (model != null) {
			dto.setExceptionTrace(model.getExceptionTrace());
			dto.setGmtCreate(model.getGmtCreate());
			dto.setGmtModified(model.getGmtModified());
			dto.setId(model.getId());
			dto.setParentTaskItem(model.getParentTaskItem());
			dto.setStatus(model.getStatus());
			dto.setTaskId(model.getTaskId());
			dto.setUrl(model.getUrl());
		}
	}
	
}
