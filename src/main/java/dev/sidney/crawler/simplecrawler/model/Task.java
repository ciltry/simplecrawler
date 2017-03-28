/**
 * 
 */
package dev.sidney.crawler.simplecrawler.model;

import dev.sidney.devutil.store.annotation.Field;
import dev.sidney.devutil.store.enums.FieldType;
import dev.sidney.devutil.store.model.BaseModel;

/**
 * @author 杨丰光 2017年3月23日16:41:25
 *
 */
public class Task extends BaseModel {

	public static final String COLUMN_TASK_NAME = "TASK_NAME";
	public static final String COLUMN_START_URL = "START_URL";
	
	/**
	 * uid
	 */
	private static final long serialVersionUID = -7968585889476164764L;
	
	@Field(comment="任务名", columnName=COLUMN_TASK_NAME)
	private String taskName;
	@Field(comment="入口url", type=FieldType.VARCHAR2, size=3000, columnName=COLUMN_START_URL, nullable=false)
	private String startUrl;
	
	
	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
}
