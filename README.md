#Spring Authorization Manager

为Api服务端添加简单的Token鉴权功能，基于Spring MVC

###功能简述

1. 对每个请求进行身份验证，如果身份验证不失败直接返回错误信息（可以自定义错误信息和Http状态码）
2. 通过鉴权信息获得当前登录的用户，并自动注入到Controller的方法中

###使用方法

仓库：

```
<repository>
    <id>scienjus-mvn-repo</id>
    <url>https://raw.github.com/ScienJus/maven/mvn-repo/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>
```

依赖：

```
<dependency>
    <groupId>com.scienjus</groupId>
    <artifactId>spring-authorization-manager</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

所有依赖库，相信大部分已经存在于你的项目中了：

```
<dependencies>
  <dependency>
  <groupId>com.scienjus</groupId>
  <artifactId>spring-authorization-manager</artifactId>
  <version>1.0-SNAPSHOT</version>
  </dependency>
  <!--Spring MVC依赖-->
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>4.2.2.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>4.2.2.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.6.3</version>
  </dependency>
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.6.3</version>
  </dependency>
  <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.1.0</version>
  </dependency>
  <!--Redis依赖，只有在使用RedisTokenManager时才需要-->
  <dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.7.3</version>
  </dependency>
  <!--数据库依赖，只有在使用DBTokenManager时才需要-->
  <dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.37</version>
  </dependency>
  <dependency>
    <groupId>commons-dbcp</groupId>
    <artifactId>commons-dbcp</artifactId>
    <version>1.4</version>
  </dependency>
</dependencies>
```

**使用Redis存储Token**

将Jedis客户端注入到`RedisTokenManager`：

```
<!--Redis配置-->
<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
</bean>

<!--Redis连接池-->
<bean id = "jedisPool" class="redis.clients.jedis.JedisPool">
  <constructor-arg index="0" ref="jedisPoolConfig"/>
  <constructor-arg index="1" value="${redis.host}"/>
  <constructor-arg index="2" value="${redis.port}" type="int"/>
  <constructor-arg index="3" value="${redis.timeout}" type="int"/>
  <constructor-arg index="4" value="${redis.password}"/>
</bean>

<!--管理验证信息的bean-->
<bean id="tokenManager" class="com.scienjus.authorization.manager.impl.RedisTokenManager">
       <!--Token失效时间-->
       <property name="tokenExpireSeconds" value="3600" />
       <!--Redis客户端-->
       <property name="jedisPool" ref="jedisPool" />
</bean>
```

**使用Database存储Token**

只需要将`RedisTokenManager`替换成`DBTokenManager`，并将数据源注入进去：

```
<!--数据源-->
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
       <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
       <property name="url" value="jdbc:mysql://127.0.0.1:3306/demo"/>
       <property name="username" value="root"/>
       <property name="password" value="root"/>
</bean>

<!--管理验证信息的bean-->
<bean id="tokenManager" class="com.scienjus.authorization.manager.impl.DBTokenManager">
       <!--Token失效时间-->
       <property name="tokenExpireSeconds" value="3600" />
       <!--数据源-->
       <property name="dataSource" ref="dataSource" />
       <!--存储验证信息的表名-->
       <property name="tableName" value="users" />
       <!--存储Key的字段名-->
       <property name="keyColumnName" value="username" />
       <!--存储Token的字段名-->
       <property name="tokenColumnName" value="token" />
       <!--存储Token过期时间的字段名-->
       <property name="expireAtColumnName" value="expire_at" />
</bean>
```

**配置身份验证的拦截器**

将配置好的`TokenManager`注入到`AuthorizationInterceptor`中：

```
<mvc:interceptors>
       <!--身份验证的拦截器-->
       <bean id="authorizationInterceptor" class="com.scienjus.authorization.interceptor.AuthorizationInterceptor">
              <!--验证信息存储的Http头-->
              <property name="httpHeaderName" value="authorization" />
              <!--验证信息通用前缀，例如Bearer-->
              <property name="httpHeaderPrefix" value="" />
              <!--验证失败时的错误信息-->
              <property name="unauthorizedErrorMessage" value="令牌失效，请重新登录" />
              <!--管理验证信息的bean-->
              <property name="manager" ref="tokenManager" />
       </bean>
</mvc:interceptors>
```

接下来只需要对需要身份验证的方法加上`@Authorization`注解即可，例如：

```
@RestController
@RequestMapping("/home")
public class TokenController {

    @RequestMapping(method = RequestMethod.GET)
    @Authorization
    public ResponseEntity<String> home() {
        return new ResponseEntity<>("Hello World", HttpStatus.OK);
    }

}
```

也可以直接在Controller类上加上该注解，这将会使该Controller中的所有方法都需要进行身份验证。

**配置获得当前登录用户的解析器**

首先需要实现`UserModelRepository`接口的`getCurrentUser`方法，可以通过Key得到对应的用户对象，然后配置一个解析器，并将其注入到`CurrentUserMethodArgumentResolver`：

```
<mvc:annotation-driven>
       <mvc:argument-resolvers>
              <!--配置注入登录用户的解析器-->
              <bean id="currentUserMethodArgumentResolver" class="com.scienjus.authorization.resolvers.CurrentUserMethodArgumentResolver">
                     <!--需要解析的用户类-->
                     <property name="userModelClass" value="com.scienjus.domain.User" />
                     <!--查询用户的bean-->
                     <property name="userModelRepository" ref="userRepository" />
              </bean>
       </mvc:argument-resolvers>
</mvc:annotation-driven>

<!--通过Key获得对应用户的bean-->
<bean id="userRepository" class="com.scienjus.repository.UserRepository" />
```

然后只需要在方法的参数上添加一个用户对象，并加上`@CurrentUser`注解，例如：

```
@RestController
@RequestMapping("/home")
public class TokenController {

    @RequestMapping(method = RequestMethod.GET)
    @Authorization
    public ResponseEntity<String> home(@CurrentUser user) {
        return new ResponseEntity<>("Hello " + user.getUsername(), HttpStatus.OK);
    }

}
```

需要注意的是，拥有`@CurrentUser`参数的方法，可以没有`@Authorization`注解，此时如果请求未登录，该参数会为`null`。

但是如果想要使用`CurrentUserMethodArgumentResolver`则必须配置`AuthorizationInterceptor`。

###更新日志

**2015-11-27**

修改了拦截器的部分代码，内容为：

1. 将返回鉴权失败信息的输出流从`response.getWriter`改为了`response.getOutputStream`，因为`@ResponseBody`默认也是用的后者，便于统一监控返回内容。
2. 可以通过配置文件自定义鉴权失败的http状态码了，默认为401（unauthorized）。
3. 将返回鉴权失败的`Content-Type`设置为`application/json`了，否则可能会导致iOS的网络库`AFNetWorking`解析报错。


###帮助

如果您在使用中遇到了问题，可以给我提 Issues，或是通过邮件联系我，我的邮箱是：`i@scienjus.com`。

源码分析见我的这篇[博客][1]

一个简单的[Demo][2]

[1]:http://www.scienjus.com/restful-token-authorization/
[2]:https://github.com/ScienJus/spring-authorization-manager-demo/
