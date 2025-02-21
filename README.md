dili-upay
  upay-shared       基础设施和共享模块（解决循环依赖问题，工具类，中间件服务mq/redis/mysql等配置，第三方框架的封装和配置，全局异常拦截配置等）
  upay-core         核心功能：安全控制、账号管理、资金管理(账户余额及收支明细)、系统配置
  upay-pipeline     第三方支付通道管理和对接，包括：微信支付、各类银行支付通道
  upay-sentinel     账号权限、资金风控管理
  upay-trade        各类交易实现：充值、提现、缴费、交易、圈存圈提等
  upay-boot         父工程（springboot打包，系统对外提供开放接口）

模块依赖
upay-shared  ->  upay-core  ->  upay-pipeline  ->  upay-trade  ->  upay-boot
                            ->  upay-sentinel
项目结构
  com.diligrp.upay.xxxx - 模块spring配置xxxxConfiguration（Spring组件扫描配置/MybatisMapper扫描配置）ErrorCode Constants
  com.diligrp.upay.xxxx.controller - 后台接口
  com.diligrp.upay.xxxx.api - 移动端接口
  com.diligrp.upay.xxxx.service
  com.diligrp.upay.xxxx.dao
  com.diligrp.upay.xxxx.exception
  com.diligrp.upay.xxxx.domain
  com.diligrp.upay.xxxx.model
  com.diligrp.upay.xxxx.type
  com.diligrp.upay.xxxx.util
  resource/com.diligrp.upay.dao.mapper - mybatis mapper文件
  
  系统对第三方系统提供接口通过upay-boot controller包
  所有数据模型类放入com.diligrp.upay.xxxx.model下，所有域模型类（VO DTO）放入com.diligrp.upay.xxxx.domain下
  所有数据模型类须继承BaseDO类，进一步规范数据表设计：需包含id version created_time modified_time
  所有枚举类型放入com.diligrp.upay.xxxx.type下，枚举类定义请提供code/name属性，参见com.diligrp.upay.shared.type.Gender
  所有自定义工具类放入com.diligrp.upay.xxxx.util下，如果大家都能公用请放upay-shared模块下
  所有异常类继承PlatformServiceException（提供了错误码和是否打印异常栈信息功能），并放入com.diligrp.upay.xxxx.exception下
  每个模块的常量类请放在模块根目录下，如通用常量请放入upay-shared模块下
  错误码为6位，每个模块的错误类ErrorCode且放入模块根目录，错误码应唯一且独特如前三位为模块标识，公共错误码参见com.diligrp.upay.shared.ErrorCode

工具类
  参见：com.diligrp.upay.shared.util.* com.diligrp.upay.shared.security.*
  包括：JsonUtils CurrencyUtils DateUtils RandomUtils AssertUtils HexUtils AesCipher RsaCipher ShaCipher KeyStoreUtils等等

技术要求
  JDK21 SpringCould SpringBoot 3版本
  编译工具：gradle
第三方库尽量使用springboot默认推荐，如：Jackson Lettuce；springboot工具集中没有推荐的第三方库，引入时请在合适模块中进行
已在upay-shared中完成Jackson配置，包括Spring DataBinding，且额外提供了Jackson工具类JsonUtils
已在upay-shared中已完成Redis基础配置Lettuce，可直接使用StringRedisTemplate，如需进行进一步封装配置请在合适的模块中配置，如需Redis分布式锁，可考虑引入Redission
已在upay-shared中已完成Mybatis基础配置，使用MapperScan完成mapper文件的扫描，不用plus，可用mybatis分页插件
已在upay-shared中完成MQ基础配置RabbitMQ，可直接进行使用RabbitTemplate且可进行Queue Exchange和消息监听器的配置
外部第三方jar放入dili-upay/libs
新技术框架的引入不以个人熟悉为重点考量标准，以技术框架的通用型和稳定性为考量标准

数据库脚本要求
  维护全量（dili-upay/scripts）和增量脚本（scripts/upgrade）
  维护增量脚本，需同时修改全量脚本
  所有建表SQL，每个字段需填写备注
  通常情况下，每个表都需要包含三个字段id，version，created_time，modified_time
  每个模块的数据表，建议统一的前缀upay_****