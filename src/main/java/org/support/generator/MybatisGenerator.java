package org.support.generator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class MybatisGenerator {

	private static File configFile;
	static {
		String path = System.getProperty("user.dir").concat("\\src\\main\\resources\\generatorConfig.xml");
		System.out.println(path);
		configFile = new File(path);
	}

	public static void main(String[] args)
			throws IOException,
					XMLParserException,
					InvalidConfigurationException,
					SQLException,
					InterruptedException {

		if(!configFile.exists()) {
			System.out.println("配置文件不存在");
			return;
		}

		List<String> warnings = new ArrayList<String>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, null, warnings);
        myBatisGenerator.generate(null);
	}
}
