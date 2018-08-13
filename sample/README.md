# 运行Sample

## 创建DB

运行DB/db.sql创建bank\_alpha和bank\_bravo数据库，表以及测试数据。

## 修改DB连接配置

修改bank-alpha-service和bank-bravo-service项目的/resources/druid.properties中的DB连接设置。

## 启动

启动eureka-server, bank-alpha-service, bank-bravo-service, bank-client

## 测试

可以运行bank-client/test/下的测试

也可以通过swagger测试：http://localhost:8000/swagger-ui.html 





