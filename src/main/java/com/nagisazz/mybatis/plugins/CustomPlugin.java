package com.nagisazz.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomPlugin extends PluginAdapter {

	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		AbstractXmlElementGenerator elementGenerator = new CustomAbstractXmlElementGenerator();
		elementGenerator.setContext(context);
		elementGenerator.setIntrospectedTable(introspectedTable);
		elementGenerator.addElements(document.getRootElement());
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		AbstractJavaMapperMethodGenerator methodGenerator = new CustomJavaMapperMethodGenerator();
		methodGenerator.setContext(context);
		methodGenerator.setIntrospectedTable(introspectedTable);
		methodGenerator.addInterfaceElements(interfaze);
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Set<FullyQualifiedJavaType> set = new HashSet<FullyQualifiedJavaType>();
		set.add(new FullyQualifiedJavaType("lombok.*"));
		set.add(new FullyQualifiedJavaType("java.time.LocalDate"));
		set.add(new FullyQualifiedJavaType("java.time.LocalDateTime"));
		set.add(new FullyQualifiedJavaType("com.nagisazz.datadict.annotation.*"));
		topLevelClass.addImportedTypes(set);
		topLevelClass.addAnnotation("@Builder");
		topLevelClass.addAnnotation("@Setter");
		topLevelClass.addAnnotation("@Getter");
		topLevelClass.addAnnotation("@NoArgsConstructor");
		topLevelClass.addAnnotation("@AllArgsConstructor");
		String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
		topLevelClass.addFileCommentLine("@DictTable(key=\"dbyw" + tableName.replace("_", "") + "\", " +
				"tableName=\"" + tableName + "\", showName=\"" + tableName.toUpperCase() + "\", desc=\"" +
				introspectedTable.getRemarks() + "\")");
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
//		String annotation = ""
		// 主键
		String fieldName = "fieldName=\"" + introspectedColumn.getActualColumnName() + "\"";
		String label = "label=\"" + introspectedColumn.getRemarks() + "\"";
		String submitName = "submitName=\"" + field.getName() + "\"";
		String required = "required=\"false\"";
		String fieldType ;
		String dbmsType;
		String extra;
		int type = introspectedColumn.getJdbcType();
		switch (type){
			// int
			case -7: case -6: case 5: case 4:
				fieldType = "fieldType=\"aty-numeric\"";
				dbmsType = "dbmsType=\"int\"";
				extra = "precision=\"0\"";
				break;
			// bigint
			case -5:
				fieldType = "fieldType=\"aty-numeric\"";
				dbmsType = "dbmsType=\"bigint\"";
				extra = "precision=\"0\"";
				break;
			// double
			case 6: case 7: case 8:
				fieldType = "fieldType=\"aty-numeric\"";
				dbmsType = "dbmsType=\"double precision\"";
				extra = "precision=\"4\"";
				break;
			// numeric,decimal
			case 2: case 3:
				fieldType = "fieldType=\"aty-numeric\"";
				dbmsType = introspectedColumn.getLength() > 9 ?
						"dbmsType=\"double precision\"" : "dbmsType=\"int\"";
				extra = introspectedColumn.getLength() > 9 ?
						"precision=\"4\"" : "precision=\"0\"";
				break;
			// varchar
			case 1: case 12:
				fieldType = "fieldType=\"aty-input\"";
				dbmsType = "dbmsType=\"varchar\"";
				extra = "maxLength=\"" + introspectedColumn.getLength() + "\"";
				break;
			// text
			case -1: case 2004: case 2005:
				fieldType = "fieldType=\"aty-textarea\"";
				dbmsType = "dbmsType=\"text\"";
				extra = "";
				break;
			// date
			case 91:
				fieldType = "fieldType=\"aty-date-picker\"";
				dbmsType = "dbmsType=\"date\"";
				extra = "showTime=\"false\", format=\"yyyy-MM-dd\", type=\"date\"";
				break;
			// datetime
			case 92: case 93:
				fieldType = "fieldType=\"aty-date-picker\"";
				dbmsType = "dbmsType=\"timestamp\"";
				extra = "showTime=\"true\", format=\"yyyy-MM-dd HH:mm:ss\", type=\"datetime\"";
				break;
			default:
				fieldType = "fieldType=\"aty-input\"";
				dbmsType = "dbmsType=\"varchar\"";
				extra = "maxLength=\"300\"";
				break;
		}
		if (introspectedColumn.getActualColumnName().equals(introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName())) {
			field.addAnnotation("@DictField(" + fieldName +
					", fieldType=\"aty-hidden\", " + dbmsType + ", " + label + ", " + submitName +
					", required=\"true\", " + extra + ", pk=true, unique=true)");
		}else {
			field.addAnnotation("@DictField(" + fieldName + ", " + fieldType + ", " + dbmsType + ", " +
					label + ", " + submitName + ", " + required + ", " + extra + ")");
		}
		return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}

	@Override
	public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		return false;
	}

	@Override
	public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		return false;
	}
}
