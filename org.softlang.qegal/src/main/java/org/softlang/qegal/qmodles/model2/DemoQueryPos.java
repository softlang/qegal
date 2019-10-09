package org.softlang.qegal.qmodles.model2;

import java.io.File;
import static org.softlang.qegal.qmodles.QModelProcessLocal.detection;

public class DemoQueryPos {

	public static void main(String[] args) {
		String datapath = "data/qmodles/model2/demo/";
		File localProjectList = new File(datapath+"demoscope.txt");
		File qegalDir = new File("src/main/java/org/softlang/qegal/qmodles/model2/rules");

		detection(datapath, localProjectList, qegalDir);
		// createStatistics(stagepath);
	}

}
