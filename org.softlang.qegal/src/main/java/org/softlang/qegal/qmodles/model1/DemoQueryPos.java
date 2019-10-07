package org.softlang.qegal.qmodles.model1;

import java.io.File;
import static org.softlang.qegal.qmodles.QModelProcessLocal.detection;

public class DemoQueryPos {

	public static void main(String[] args) {
		String datapath = "data/qmodles/model1/demo/";
		File localProjectList = new File("src/main/java/org/softlang/qegal/qmodles/model1/demoscope.txt");
		File qegalDir = new File("src/main/java/org/softlang/qegal/qmodles/model1");

		detection(datapath, localProjectList, qegalDir);
		// createStatistics(stagepath);
	}

}
