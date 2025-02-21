package com.diligrp.upay.sentinel.service.impl;

import com.diligrp.upay.sentinel.Constants;
import com.diligrp.upay.sentinel.domain.ExecuteContext;
import com.diligrp.upay.sentinel.domain.Passport;
import com.diligrp.upay.sentinel.service.ISentinelAssistant;
import com.diligrp.upay.shared.redis.LettuceTemplate;
import com.diligrp.upay.shared.redis.LettuceTemplate.TransactionCallback;
import com.diligrp.upay.shared.util.DateUtils;
import com.diligrp.upay.shared.util.NumberUtils;
import io.lettuce.core.TransactionResult;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * 默认风控引擎执行助手
 */
@Service("sentinelAssistant")
public class DefaultSentinelAssistant implements ISentinelAssistant {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSentinelAssistant.class);

    @Resource
    private LettuceTemplate<String, String> lettuceTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecuteContext loadWithdrawExecuteContext(Passport passport) {
        ExecuteContext context = new ExecuteContext();
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, Constants.YYYYMMDD);
        String month = DateUtils.formatDate(now, Constants.YYYYMM);

        TransactionCallback<String, String> transactionCallback = command -> {
            // upay:sentinel:withdraw:{accountId}:dailyAmount:{today}
            String dailyAmountKey = String.format(Constants.SENTINEL_WITHDRAW_DAILYAMOUNT, passport.getAccountId(), today);
            command.get(dailyAmountKey);
            // upay:sentinel:withdraw:{accountId}:dailyTimes:{today}
            String dailyTimesKey = String.format(Constants.SENTINEL_WITHDRAW_DAILYTIMES, passport.getAccountId(), today);
            command.get(dailyTimesKey);
            // upay:sentinel:withdraw:{accountId}:monthlyAmount:{month}
            String monthlyAmountKey = String.format(Constants.SENTINEL_WITHDRAW_MONTHLYAMOUNT, passport.getAccountId(), month);
            command.get(monthlyAmountKey);
            // 使用pipeline模式一次交互获取所需值，优化访问性能
            TransactionResult result = command.exec();
            long dailyAmount = NumberUtils.str2Long(result.get(0), 0);
            int dailyTimes = NumberUtils.str2Int(result.get(1), 0);
            long monthlyAmount = NumberUtils.str2Long(result.get(2), 0);
            context.setDailyAmount(dailyAmount);
            context.setDailyTimes(dailyTimes);
            context.setMonthlyAmount(monthlyAmount);
        };

        try {
            lettuceTemplate.execute(transactionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: load withdraw execute context from redis error", ex);
        }

        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshWithdrawExecuteContext(Passport passport) {
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, Constants.YYYYMMDD);
        String thisMonth = DateUtils.formatDate(now, Constants.YYYYMM);
        String yesterday = DateUtils.formatDate(now.minusDays(1), Constants.YYYYMMDD);
        String lastMonth = DateUtils.formatDate(now.minusMonths(1), Constants.YYYYMM);

        TransactionCallback<String, String> transactionCallback = command -> {
            // upay:sentinel:withdraw:{accountId}:dailyAmount:{today}
            String dailyAmountKey = String.format(Constants.SENTINEL_WITHDRAW_DAILYAMOUNT, passport.getAccountId(), today);
            command.incrby(dailyAmountKey, passport.getAmount());
            command.expire(dailyAmountKey, Constants.ONE_DAY_SECONDS);
            // upay:sentinel:withdraw:{accountId}:dailyTimes:{today}
            String dailyTimesKey = String.format(Constants.SENTINEL_WITHDRAW_DAILYTIMES, passport.getAccountId(), today);
            command.incrby(dailyTimesKey, 1);
            command.expire(dailyTimesKey, Constants.ONE_DAY_SECONDS);
            // 获取当月最后一天，并计算间隔天数，将间隔天数+1作为月提现金额的过期时间
            LocalDate lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
            int days = (int)now.until(lastDay, ChronoUnit.DAYS) + 1;
            // upay:sentinel:withdraw:{accountId}:monthlyAmount:{month}
            String monthlyAmountKey = String.format(Constants.SENTINEL_WITHDRAW_MONTHLYAMOUNT, passport.getAccountId(), thisMonth);
            command.incrby(monthlyAmountKey, passport.getAmount());
            command.expire(monthlyAmountKey, Constants.ONE_DAY_SECONDS * days);
            // 清理上个时间周期的缓存值，优化Redis存储
            String lastDailyAmountKey = String.format(Constants.SENTINEL_WITHDRAW_DAILYAMOUNT, passport.getAccountId(), yesterday);
            command.del(lastDailyAmountKey);
            String lastDailyTimesKey = String.format(Constants.SENTINEL_WITHDRAW_DAILYTIMES, passport.getAccountId(), yesterday);
            command.del(lastDailyTimesKey);
            String lastMonthlyAmountKey = String.format(Constants.SENTINEL_WITHDRAW_MONTHLYAMOUNT, passport.getAccountId(), lastMonth);
            command.del(lastMonthlyAmountKey);

            command.exec();
        };

        try {
            lettuceTemplate.execute(transactionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: refresh withdraw execute context into redis error", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecuteContext loadTradeExecuteContext(Passport passport) {
        ExecuteContext context = new ExecuteContext();
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, Constants.YYYYMMDD);
        String month = DateUtils.formatDate(now, Constants.YYYYMM);

        TransactionCallback<String, String> transactionCallback = command -> {
            // upay:sentinel:trade:{accountId}:dailyAmount:{today}
            String dailyAmountKey = String.format(Constants.SENTINEL_TRADE_DAILYAMOUNT, passport.getAccountId(), today);
            command.get(dailyAmountKey);
            // upay:sentinel:trade:{accountId}:dailyTimes:{today}
            String dailyTimesKey = String.format(Constants.SENTINEL_TRADE_DAILYTIMES, passport.getAccountId(), today);
            command.get(dailyTimesKey);
            // upay:sentinel:trade:{accountId}:monthlyAmount:{month}
            String monthlyAmountKey = String.format(Constants.SENTINEL_TRADE_MONTHLYAMOUNT, passport.getAccountId(), month);
            command.get(monthlyAmountKey);
            // 使用pipeline模式一次交互获取所需值，优化访问性能
            TransactionResult result = command.exec();
            long dailyAmount = NumberUtils.str2Long(result.get(0), 0);
            int dailyTimes = NumberUtils.str2Int(result.get(1), 0);
            long monthlyAmount = NumberUtils.str2Long(result.get(2), 0);
            context.setDailyAmount(dailyAmount);
            context.setDailyTimes(dailyTimes);
            context.setMonthlyAmount(monthlyAmount);
        };

        try {
            lettuceTemplate.execute(transactionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: load trade execute context from redis error", ex);
        }

        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshTradeExecuteContext(Passport passport) {
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, Constants.YYYYMMDD);
        String thisMonth = DateUtils.formatDate(now, Constants.YYYYMM);
        String yesterday = DateUtils.formatDate(now.minusDays(1), Constants.YYYYMMDD);
        String lastMonth = DateUtils.formatDate(now.minusMonths(1), Constants.YYYYMM);

        TransactionCallback<String, String> transactionCallback = command -> {
            // upay:sentinel:trade:{accountId}:{today}:dailyAmount
            String dailyAmountKey = String.format(Constants.SENTINEL_TRADE_DAILYAMOUNT, passport.getAccountId(), today);
            command.incrby(dailyAmountKey, passport.getAmount());
            command.expire(dailyAmountKey, Constants.ONE_DAY_SECONDS);
            // upay:sentinel:trade:{accountId}:dailyTimes:{today}
            String dailyTimesKey = String.format(Constants.SENTINEL_TRADE_DAILYTIMES, passport.getAccountId(), today);
            command.incrby(dailyTimesKey, 1);
            command.expire(dailyTimesKey, Constants.ONE_DAY_SECONDS);
            // 获取当月最后一天，并计算间隔天数，将间隔天数+1作为月提现金额的过期时间
            LocalDate lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
            int days = (int)now.until(lastDay, ChronoUnit.DAYS) + 1;
            // upay:sentinel:trade:{accountId}:monthlyAmount:{month}
            String monthlyAmountKey = String.format(Constants.SENTINEL_TRADE_MONTHLYAMOUNT, passport.getAccountId(), thisMonth);
            command.incrby(monthlyAmountKey, passport.getAmount());
            command.expire(monthlyAmountKey, Constants.ONE_DAY_SECONDS * days);
            // 清理上个时间周期的缓存值，优化Redis存储
            String lastDailyAmountKey = String.format(Constants.SENTINEL_TRADE_DAILYAMOUNT, passport.getAccountId(), yesterday);
            command.del(lastDailyAmountKey);
            String lastDailyTimesKey = String.format(Constants.SENTINEL_TRADE_DAILYTIMES, passport.getAccountId(), yesterday);
            command.del(lastDailyTimesKey);
            String lastMonthlyAmountKey = String.format(Constants.SENTINEL_TRADE_MONTHLYAMOUNT, passport.getAccountId(), lastMonth);
            command.del(lastMonthlyAmountKey);

            command.exec();
            LOG.debug("风控提示-资金账号: {} 发生交易额: {}", passport.getAccountId(), passport.getAmount());
        };

        try {
            lettuceTemplate.execute(transactionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: refresh trade execute context into redis error", ex);
        }
    }
}
