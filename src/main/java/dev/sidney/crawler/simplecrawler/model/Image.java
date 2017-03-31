/**
 * 
 */
package dev.sidney.crawler.simplecrawler.model;

import dev.sidney.devutil.store.annotation.Field;
import dev.sidney.devutil.store.annotation.FieldMapping;
import dev.sidney.devutil.store.annotation.Model;
import dev.sidney.devutil.store.enums.FieldType;
import dev.sidney.devutil.store.model.BaseModel;

/**
 * @author 杨丰光 2017年3月31日11:23:37
 *
 */
@Model(tableName="IMAGE")
public class Image extends BaseModel {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -8812929138080467091L;
	
	@FieldMapping("url")
	public static final String COLUMN_URL = "URL";
	@FieldMapping("taskId")
	public static final String COLUMN_TASK_ID = "TASK_ID";
	
	@Field(comment="任务ID", type=FieldType.CHAR, columnName = COLUMN_TASK_ID, size=36, nullable=false)
	private String taskId;

	@Field(comment="url", type=FieldType.VARCHAR2, size=3000, columnName=COLUMN_URL, nullable=false)
	private String url;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
