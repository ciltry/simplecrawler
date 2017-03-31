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
 * @author 杨丰光 2017年3月23日14:49:36
 *
 */
@Model(tableName="TASK_ITEM")
public class TaskItem extends BaseModel {

	@FieldMapping("status")
	public static final String COLUMN_STATUS = "STATUS";
	@FieldMapping("url")
	public static final String COLUMN_URL = "URL";
	@FieldMapping("taskId")
	public static final String COLUMN_TASK_ID = "TASK_ID";
	@FieldMapping("type")
	public static final String COLUMN_TYPE = "TYPE";
	
	/**
	 * uid
	 */
	private static final long serialVersionUID = -5625541371152780731L;
	@Field(comment="任务ID", type=FieldType.CHAR, columnName = COLUMN_TASK_ID, size=36, nullable=false)
	private String taskId;
	@Field(comment="父ID", type=FieldType.CHAR, size=36)
	private String parentTaskItem;
	@Field(comment="url", type=FieldType.VARCHAR2, size=3000, columnName=COLUMN_URL, nullable=false)
	private String url;
	@Field(comment="状态 I: 初始状态, P: 处理中, E:异常，S: 结束", type=FieldType.CHAR, size=1, columnName=COLUMN_STATUS, nullable = false)
	private String status;
	@Field(comment="异常追踪", type=FieldType.VARCHAR2, size = 4000)
	private String exceptionTrace;
	@Field(comment="任务类型  IMG,HTML", type=FieldType.VARCHAR2, size=20, columnName=COLUMN_TYPE)
	private String type;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
