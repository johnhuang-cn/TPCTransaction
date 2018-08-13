# 2PC Transaction Framework

TPCTransaction是依据两阶段提交理论实现的分布式事务框架。以转帐事务为例，其实现原理如下：

![](/assets/Diagram.png)

## 实现要点

1. 被调用方的业务按正常本地事务实现，TPCTransaction框架会根据注解拦截该接口的调用，将该事务转为异步事务，将完成全部数据库操作即commit前将事务锁住，并将结果提交至主线程正常返回
2. 发起方完成各个服务的调用后，如果一切正常，将向所有参与方同时发起Commit请求。参与方收到Commit请求后，解锁异步事务，完成commit。
3. 若发起方遇到错误需要回滚，则向所有已参与方同时发起Rollback请求。参与方收到Rollback请求后，解锁异步事务，完成rollback。

## 主要特点

1. 对原业务代码几乎零侵入，只需在原业务方法上加一个@TPCTransactional注解
2. 性能只比无事务控制的一阶段提交损失15-20%
3. 代码调用直观，和正常远程调用基本一致

## 示例代码

### 发起方

```
@Autowired
private TPCTransactionManager tm;


try {
    tm.timeout(5).begin();
    alphaAccountService.changeAmountForUserId4TPC(userId, -amount);
    bravoAccountService.changeAmountForUserId4TPC(userId, amount);
    tm.commit(IAlphaAccountService.class, IBravoAccountService.class);
    isSuccess = true;
} catch (Exception e) {
    tm.rollback(e, IAlphaAccountService.class, IBravoAccountService.class);
    err = e.getMessage();
}
```

### 服务方

```
@TPCTransactional
public void transferIn4TPC(String userId, double amount) throws Exception {
    int count = mapper.increaseAmount(userId, amount);
}
```

## 使用指南

### 依赖

下载本工程，导入TPC工程，并执行mvn install

下载snowflake工程：[https://github.com/johnhuang-cn/snowflake-uid](https://github.com/johnhuang-cn/snowflake-uid)，并执行mvn install。本项目使用它生成全局事务唯一ID。

### 设置DataSource和TransactionManager

```
@Configuration
public class DSConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.druid.alpha")
    public DataSource dataSource(){
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager transactionManager (DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```

### EnableTPC

在Spring Boot启动类加上@EnableTPC注解：

```
@SpringBootApplication
@EnableEurekaClient
@EnableTPC
public class BankBravoApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankBravoApplication.class, args);
    }
}
```

### 发起方示例代码

参见：Sample/bank-client工程的AccountController.transferTPC\(\)  
[https://github.com/johnhuang-cn/TPCTransaction/blob/master/sample/bank-client/src/main/java/net/xdevelop/template/bank/controller/AccountController.java\#L115](https://github.com/johnhuang-cn/TPCTransaction/blob/master/sample/bank-client/src/main/java/net/xdevelop/template/bank/controller/AccountController.java#L115)

### 服务方示例代码

参见：Sample/bank-alpha-service工程的AccountService.transferIn4TPC\(\)

[https://github.com/johnhuang-cn/TPCTransaction/blob/master/sample/bank-alpha-service/src/main/java/net/xdevelop/template/bank/service/AccountService.java\#L54](https://github.com/johnhuang-cn/TPCTransaction/blob/master/sample/bank-alpha-service/src/main/java/net/xdevelop/template/bank/service/AccountService.java#L54)

