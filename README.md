# mybatis-generator-plus
mybatis-generator扩展插件，新增多种方法，完善插件功能
> 欢迎各位多提issue，看到会及时处理，也可以直接邮箱联系597306107@qq.com
- 如何使用
    - 打包至本地maven仓库
    - 在项目pom中添加插件
    ```xml
      <plugin>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-maven-plugin</artifactId>
        <version>1.3.5</version>
        <configuration>
            <configurationFile>src/main/resources/generatorConfig.xml</configurationFile>
            <verbose>true</verbose>
            <overwrite>true</overwrite>
        </configuration>
        <dependencies>
            <dependency>
                <groupId>com.nagisazz</groupId>
                <artifactId>mybatis-generator-plus</artifactId>
                <version>1.1</version>
            </dependency>
        </dependencies>
      </plugin>
    ```
    - 复制generatorConfig.xml至项目中，修改generatorConfig.xml中数据源及table节点
    - 运行插件
- 新增方法
    - selectOne：根据传入的实体非null字段查询单条记录
    - selectList：根据传入的实体非null字段查询多条记录
    - deleteSelective：根据传入的实体非null字段删除记录
    - batchUpdate：批量更新全量字段
    - batchUpdateSelective：批量更新传入参数非null字段
    - batchInsert：批量插入
    - batchDelete：批量删除
    - batchSelect：根据传入的非null字段批量查询
- 新增功能
    - 根据数据库注释生成entity注释
    - 在entity上增加lombok注解，包括@Builder、@Setter、@Getter、@NoArgsConstructor、@AllArgsConstructor
- 完善功能
    - 将TIMESTAMP映射成LocalDateTime，将Date映射成LocalDate，将符合标准的浮点数正确映射成Double
    - 将默认生成的方法参数名从record改成对象名小写