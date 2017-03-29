package dev.sidney.crawler.simplecrawler.datacapture;

import java.util.List;

import dev.sidney.crawler.simplecrawler.dto.TaskItemDTO;

public interface DataCapture<T> {

	public abstract List<T> captureData(TaskItemDTO taskItem, String input);

}