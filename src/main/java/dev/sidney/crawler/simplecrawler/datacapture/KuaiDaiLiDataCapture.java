/**
 * 
 */
package dev.sidney.crawler.simplecrawler.datacapture;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import dev.sidney.proxypool.dto.ServerDTO;

/**
 * @author Sidney
 *
 */
@Service("KuaiDaiLiDataCapture")
public class KuaiDaiLiDataCapture extends BaseDataCapture<ServerDTO> {
	
	@Override
	public List<ServerDTO> buildData(String captureKey, String[] dataList) {
		List<ServerDTO> list = null;
		if ("表格".equals(captureKey)) {
			list = this.captureData("行", Pattern.compile("<tr>[\\w\\W]*?</tr>"), dataList[0]);
		} else if ("行".equals(captureKey)) {
			list = this.captureData("服务器", Pattern.compile("\"IP\">([^<]*?)</td>[\\w\\W]*?\"PORT\">([^<]*?)</td>[\\w\\W]*?\"匿名度\">([^<]*?)</td>[\\w\\W]*?\"类型\">([^<]*?)</td>[\\w\\W]*?\"位置\">([^<]*?)</td>[\\w\\W]*?\"响应速度\">([^<]*?)</td>[\\w\\W]*?\"最后验证时间\">([^<]*?)</td>"), dataList[0]);
		} else if ("服务器".equals(captureKey)) {
			list = new ArrayList<ServerDTO>();
			ServerDTO server = new ServerDTO();
			server.setIp(dataList[1]);
			server.setPort(Integer.parseInt(dataList[2]));
			server.setType(dataList[4]);
			server.setLocation(dataList[5]);
			server.setSpeed((int) (Double.parseDouble(dataList[6].replaceAll("秒", "")) * 1000));
			server.setGmtLastTest(null);
			list.add(server);
		}
		return list;
	}

	@Override
	public List<ServerDTO> captureData(String input) {
		Pattern pt = Pattern.compile("<div\\s+id=\"list\"[\\w\\W]*?</div>");
		return this.captureData("表格", pt, input);
	}

}
