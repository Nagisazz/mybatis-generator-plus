package com.nagisazz.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;

import java.util.Set;
import java.util.TreeSet;

public class CustomJavaMapperMethodGenerator extends AbstractJavaMapperMethodGenerator {

    @Override
    public void addInterfaceElements(Interface interfaze) {

        addMethod("selectOne", false, false, false, false, interfaze);
        addMethod("selectList", false, true, false, false, interfaze);
        addMethod("deleteSelective", true, false, false, false, interfaze);
        if (introspectedTable.getPrimaryKeyColumns() != null) {
            addMethod("batchUpdate", true, false, true, false, interfaze);
            addMethod("batchUpdateSelective", true, false, true, false, interfaze);
            addMethod("batchInsert", true, false, true, false, interfaze);
            addMethod("batchDelete", true, false, true, true, interfaze);
            addMethod("batchSelect", false, true, true, true, interfaze);
        }
    }

    /**
     * 添加方法
     *
     * @param methodName
     * @param isReturnInt
     * @param isReturnList
     * @param isParameterList
     * @param isParameterListStr
     * @param interfaze
     */
    private void addMethod(String methodName, boolean isReturnInt, boolean isReturnList, boolean isParameterList, boolean isParameterListStr, Interface interfaze) {

        // 定义 parameter 和 list parameter
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(parameterType);

        // 定义 liststr parameter
        FullyQualifiedJavaType listStrType = FullyQualifiedJavaType.getNewListInstance();
        if (introspectedTable.getPrimaryKeyColumns() != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
            listStrType.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType());
        }

        // 先创建import对象
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        // import List的包
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        // import 参数类型对象
        importedTypes.add(parameterType);
        // 创建方法对象，设置该方法为public
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);

        // 设置返回类型
        method.setReturnType(isReturnInt ? FullyQualifiedJavaType.getIntInstance() : isReturnList ? listType : parameterType);
        // 设置方法名称
        method.setName(methodName);

        // 设置参数
        String record = parameterType.getShortName().toLowerCase();
        String list = parameterType.getShortName().toLowerCase() + "List";
        method.addParameter(isParameterList ? isParameterListStr ? new Parameter(listStrType, list) : new Parameter(listType, list) :
                new Parameter(parameterType, record));
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        if (context.getPlugins().clientSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable)) {
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }
}
