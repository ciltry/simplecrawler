/**
 * 
 */
package dev.sidney.crawler.simplecrawler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import dev.sidney.crawler.simplecrawler.datacapture.DataCapture;
import dev.sidney.crawler.simplecrawler.datacapture.KuaiDaiLiDataCapture;
import dev.sidney.proxypool.dto.ServerDTO;

/**
 * @author Sidney
 *
 */
public class KuaiDaiLiDataCaptureTest {

	@Test
	public void test1() throws IOException {
		DataCapture capture = new KuaiDaiLiDataCapture();
		String str = FileUtils.readFileToString(new File("src\\test\\resources\\a.txt"), "utf-8");
		List<ServerDTO> list = capture.captureData(str);
		System.out.println(list.size());
		System.out.println(list);
	}
}
