/**
 * 
 */
package dev.sidney.crawler.simplecrawler.enums;

/**
 * @author 杨丰光 2017年3月23日16:57:01
 *
 */
public enum TaskItemStatusEnum {

	INITIAL("I", "初始状态"),
	EXCEPTIONAL("E", "异常"),
	SUCCESS("S", "正常结束"),
	IGNORE("G", "忽略"),
	PROCESSING("P", "执行中");
	
	
	private TaskItemStatusEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	
	
	private String code;
	private String desc;
	
	
	public static TaskItemStatusEnum getByCode(String code) {
		TaskItemStatusEnum res = null;
		for (TaskItemStatusEnum e: TaskItemStatusEnum.values()) {
			if (e.getCode().equals(code)) {
				res = e;
				break;
			}
		}
		return res;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
