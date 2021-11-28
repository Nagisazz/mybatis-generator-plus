package com.nagisazz.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.LinkedList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class CustomAbstractXmlElementGenerator extends AbstractXmlElementGenerator {

	@Override
	public void addElements(XmlElement parentElement) {
		addSelectElements(parentElement);
		addBatchUpdateElements(parentElement);
		addBatchUpdateSelectiveElements(parentElement);
		addBatchInsertElements(parentElement);
		addBatchDeleteElements(parentElement);
		addBatchSelectElements(parentElement);
		addDeleteSelectiveElements(parentElement);
	}

    private void addDeleteSelectiveElements(XmlElement parentElement) {

        XmlElement answer = new XmlElement("delete");
        answer.addAttribute(new Attribute("id", "deleteSelective"));

        StringBuilder deleteStr = new StringBuilder();
        deleteStr.append("delete from ");
        deleteStr.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());

        // 公用includeQuery
        XmlElement includeQuery = new XmlElement("include");
        includeQuery.addAttribute(new Attribute("refid", "BASE_QUERY"));

        // 拼接
		answer.addElement(new TextElement(deleteStr.toString()));
		answer.addElement(includeQuery);
		parentElement.addElement(answer);
    }

    private void addSelectElements(XmlElement parentElement){
		// 增加BASE_QUERY
		XmlElement sql = new XmlElement("sql");
		sql.addAttribute(new Attribute("id", "BASE_QUERY"));
		//在这里添加where条件
		XmlElement selectTrimElement = new XmlElement("trim"); //设置trim标签
		selectTrimElement.addAttribute(new Attribute("prefix", "WHERE"));
		selectTrimElement.addAttribute(new Attribute("prefixOverrides", "AND|OR")); //添加where和and
		StringBuilder sb = new StringBuilder();
		for(IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
			XmlElement selectNotNullElement = new XmlElement("if"); //$NON-NLS-1$
			sb.setLength(0);
			sb.append("null != ");
			sb.append(introspectedColumn.getJavaProperty());
			selectNotNullElement.addAttribute(new Attribute("test", sb.toString()));
			sb.setLength(0);
			// 添加and
			sb.append(" and ");
			sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			// 添加等号
			sb.append(" = ");
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
			selectNotNullElement.addElement(new TextElement(sb.toString()));
			selectTrimElement.addElement(selectNotNullElement);
		}
		sql.addElement(selectTrimElement);
		parentElement.addElement(sql);

		// 公用select
		sb.setLength(0);
		sb.append("select ");
		TextElement selectText = new TextElement(sb.toString());

		// 公用selectFrom
		sb.setLength(0);
		sb.append("from ");
		sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
		TextElement selectFromText = new TextElement(sb.toString());

		// 公用includeColumn
		XmlElement includeColumn = new XmlElement("include");
		includeColumn.addAttribute(new Attribute("refid", "Base_Column_List"));

		// 公用includeQuery
		XmlElement includeQuery = new XmlElement("include");
		includeQuery.addAttribute(new Attribute("refid", "BASE_QUERY"));

		// findOneLimit
		sb.setLength(0);
		sb.append("limit 1");
		TextElement selectTextLimit = new TextElement(sb.toString());

		// 增加find
		XmlElement find = new XmlElement("select");
		find.addAttribute(new Attribute("id", "selectOne"));
		find.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		find.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
		find.addElement(selectText);
		find.addElement(includeColumn);
		find.addElement(selectFromText);
		find.addElement(includeQuery);
		find.addElement(selectTextLimit);
		parentElement.addElement(find);

		// 增加list
		XmlElement list = new XmlElement("select");
		list.addAttribute(new Attribute("id", "selectList"));
		list.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		list.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
		list.addElement(selectText);
		list.addElement(includeColumn);
		list.addElement(selectFromText);
		list.addElement(includeQuery);
		parentElement.addElement(list);
	}

	private void addBatchUpdateElements(XmlElement parentElement) {
		addBatchUpdateElements(parentElement, false);
	}

	private void addBatchUpdateSelectiveElements(XmlElement parentElement) {
		addBatchUpdateElements(parentElement, true);
	}

	private void addBatchUpdateElements(XmlElement parentElement, boolean selective) {

		if (introspectedTable.getPrimaryKeyColumns() == null){
			return;
		}

		XmlElement answer = new XmlElement("update");
		if (selective) {
			answer.addAttribute(new Attribute("id", "batchUpdateSelective"));
		} else {
			answer.addAttribute(new Attribute("id", "batchUpdate"));
		}
		answer.addAttribute(new Attribute("parameterType", "java.util.List"));

		// 外层foreach
		XmlElement foreachElement = new XmlElement("foreach");
		foreachElement.addAttribute(new Attribute("collection", "list"));
		foreachElement.addAttribute(new Attribute("item", "item"));
		foreachElement.addAttribute(new Attribute("separator", ";"));

		// update table
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
		foreachElement.addElement(new TextElement(sb.toString()));

		// set标签
		XmlElement setElement = new XmlElement("set");

		// 添加字段
		IntrospectedColumn primaryKeyColumn = introspectedTable.getPrimaryKeyColumns().get(0);
		int index = 1;
		for (IntrospectedColumn introspectedColumn : ListUtilities.removeGeneratedAlwaysColumns(introspectedTable
				.getNonPrimaryKeyColumns())) {

			if (introspectedColumn.isSequenceColumn()
					|| introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
				// if it is a sequence column, it is not optional
				// This is required for MyBatis3 because MyBatis3 parses
				// and calculates the SQL before executing the selectKey

				// if it is primitive, we cannot do a null check

				continue;
			}

			sb.setLength(0);
			sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			sb.append(" = ");
			sb.append(getItemParameterClause(introspectedColumn));
			if (index != ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()).size()){
				sb.append(",");
			}
			TextElement textElement = new TextElement(sb.toString());

			// 添加 if null 判断
			if (selective) {
				XmlElement ifElement = new XmlElement("if");
				sb.setLength(0);
				sb.append("item.");
				sb.append(introspectedColumn.getJavaProperty());
				sb.append(" != null");
				ifElement.addAttribute(new Attribute("test", sb.toString()));
				ifElement.addElement(textElement);
				setElement.addElement(ifElement);
			} else {
				setElement.addElement(textElement);
			}
			index++;
		}
		foreachElement.addElement(setElement);

		// where id =
		sb.setLength(0);
		sb.append("where ");
		sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(primaryKeyColumn));
		sb.append(" = ");
		sb.append(getItemParameterClause(primaryKeyColumn));
		foreachElement.addElement(new TextElement(sb.toString()));

		answer.addElement(foreachElement);

		parentElement.addElement(answer);
	}

	private void addBatchInsertElements(XmlElement parentElement) {

		XmlElement answer = new XmlElement("insert");
		answer.addAttribute(new Attribute("id", "batchInsert"));
		answer.addAttribute(new Attribute("parameterType", "java.util.List"));

		StringBuilder insertClause = new StringBuilder();
		insertClause.append("insert into ");
		insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
		insertClause.append(" (");
		StringBuilder valuesClause = new StringBuilder();
		valuesClause.append("(");

		List<String> valuesClauses = new LinkedList<>();
		List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
		for (int i = 0; i < columns.size(); i++) {
			IntrospectedColumn introspectedColumn = columns.get(i);

			insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			valuesClause.append(getItemParameterClause(introspectedColumn));
			if (i + 1 < columns.size()) {
				insertClause.append(", ");
				valuesClause.append(", ");
			}

			if (valuesClause.length() > 80) {
				answer.addElement(new TextElement(insertClause.toString()));
				insertClause.setLength(0);
				OutputUtilities.xmlIndent(insertClause, 1);

				valuesClauses.add(valuesClause.toString());
				valuesClause.setLength(0);
				OutputUtilities.xmlIndent(valuesClause, 1);
			}
		}

		insertClause.append(')');
		answer.addElement(new TextElement(insertClause.toString()));

		answer.addElement(new TextElement("values"));

		valuesClause.append(')');
		valuesClauses.add(valuesClause.toString());

		XmlElement foreach = new XmlElement("foreach");
		foreach.addAttribute(new Attribute("collection", "list"));
		foreach.addAttribute(new Attribute("item", "item"));
		foreach.addAttribute(new Attribute("separator", ","));

		for (String clause : valuesClauses) {
			foreach.addElement(new TextElement(clause));
		}

		answer.addElement(foreach);

		parentElement.addElement(answer);
	}

	private void addBatchDeleteElements(XmlElement parentElement) {

		if (introspectedTable.getPrimaryKeyColumns() == null){
			return;
		}
		// 主键字段
		IntrospectedColumn primaryKeyColumn = introspectedTable.getPrimaryKeyColumns().get(0);

		XmlElement answer = new XmlElement("delete");
		answer.addAttribute(new Attribute("id", "batchDelete"));
//		answer.addAttribute(new Attribute("parameterType", "java.util.List"));

		StringBuilder deleteStr = new StringBuilder();
		deleteStr.append("delete from ");
		deleteStr.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
		deleteStr.append(" where ");
		deleteStr.append(MyBatis3FormattingUtilities.getEscapedColumnName(primaryKeyColumn));
		deleteStr.append(" in");
		answer.addElement(new TextElement(deleteStr.toString()));

		XmlElement foreach = new XmlElement("foreach");
		foreach.addAttribute(new Attribute("collection", "list"));
		foreach.addAttribute(new Attribute("item", "item"));
		foreach.addAttribute(new Attribute("separator", ","));
		foreach.addAttribute(new Attribute("open", "("));
		foreach.addAttribute(new Attribute("close", ")"));

		foreach.addElement(new TextElement("#{item}"));
		answer.addElement(foreach);
		parentElement.addElement(answer);
	}
	private void addBatchSelectElements(XmlElement parentElement) {

		if (introspectedTable.getPrimaryKeyColumns() == null){
			return;
		}
		// 主键字段
		IntrospectedColumn primaryKeyColumn = introspectedTable.getPrimaryKeyColumns().get(0);

		XmlElement answer = new XmlElement("select");
		answer.addAttribute(new Attribute("id", "batchSelect"));
		answer.addAttribute(new Attribute("resultMap", "BaseResultMap"));

        StringBuilder sb = new StringBuilder();
        // 公用select
        sb.setLength(0);
        sb.append("select ");
        TextElement selectText = new TextElement(sb.toString());

        // 公用selectFrom
        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        TextElement selectFromText = new TextElement(sb.toString());

        // 公用includeColumn
        XmlElement includeColumn = new XmlElement("include");
        includeColumn.addAttribute(new Attribute("refid", "Base_Column_List"));

        StringBuilder selectStr = new StringBuilder();
        selectStr.append(" where ");
        selectStr.append(MyBatis3FormattingUtilities.getEscapedColumnName(primaryKeyColumn));
        selectStr.append(" in");

        // 拼接
        answer.addElement(selectText);
        answer.addElement(includeColumn);
        answer.addElement(selectFromText);
        answer.addElement(new TextElement(selectStr.toString()));

		XmlElement foreach = new XmlElement("foreach");
		foreach.addAttribute(new Attribute("collection", "list"));
		foreach.addAttribute(new Attribute("item", "item"));
		foreach.addAttribute(new Attribute("separator", ","));
		foreach.addAttribute(new Attribute("open", "("));
		foreach.addAttribute(new Attribute("close", ")"));

		foreach.addElement(new TextElement("#{item}"));
		answer.addElement(foreach);
		parentElement.addElement(answer);
	}

	private String getItemParameterClause(IntrospectedColumn introspectedColumn) {
		return getItemParameterClause(introspectedColumn, null);
	}

	private String getItemParameterClause(IntrospectedColumn introspectedColumn, String prefix) {
		StringBuilder sb = new StringBuilder();

		sb.append("#{item."); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty(prefix));
		sb.append(",jdbcType="); //$NON-NLS-1$
		sb.append(introspectedColumn.getJdbcTypeName());

		if (stringHasValue(introspectedColumn.getTypeHandler())) {
			sb.append(",typeHandler="); //$NON-NLS-1$
			sb.append(introspectedColumn.getTypeHandler());
		}

		sb.append('}');

		return sb.toString();
	}



}
