package dev.sidney.crawler.simplecrawler.datacapture;

import java.util.List;

public interface DataCapture<T> {

	public abstract List<T> captureData(String input);

}