package com.diligrp.upay.sentinel.domain;

import com.diligrp.upay.sentinel.exception.RiskControlException;
import com.diligrp.upay.sentinel.type.PassportType;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.service.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 风险控制领域模型
 */
public class RiskControlEngine {

    private static final Logger LOG = LoggerFactory.getLogger(RiskControlEngine.class);

    private final RiskControlContext context;

    // 用户权限值
    private int permission;

    public RiskControlEngine(RiskControlContext context) {
        this.context = context;
    }

    public RiskControlContext context() {
        if (this.context == null) {
            throw new RiskControlException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "风控功能配置错误");
        }

        return this.context;
    }

    public RiskControlEngine forPermission(int permission) {
        this.permission = permission;
        return this;
    }

    public RiskControlEngine forDeposit(DepositPermission permission) {
        context().forDeposit(new DepositSentinel(permission));
        return this;
    }

    public RiskControlEngine forWithdraw(WithdrawPermission permission) {
        context().forWithdraw(new WithdrawSentinel(permission));
        return this;
    }

    public RiskControlEngine forTrade(TradePermission permission) {
        context().forTrade(new TradeSentinel(permission));
        return this;
    }

    public void checkPassport(Passport passport) {
        RiskControlContext context = context();
        passport.setPermission(permission);
        if (passport.getType() == PassportType.FOR_DEPOSIT) {
            context.forDeposit().checkPassport(passport);
        } else if (passport.getType() == PassportType.FOR_WITHDRAW) {
            context.forWithdraw().checkPassport(passport);
        } else if (passport.getType() == PassportType.FOR_TRADE) {
            context.forTrade().checkPassport(passport);
        }
    }

    public void admitPassport(Passport passport) {
        RiskControlContext context = context();
        // 异步执行, 以便提升程序性能
        ThreadPoolService.getIoThreadPoll().submit(() -> {
            try {
                if (passport.getType() == PassportType.FOR_DEPOSIT) {
                    context.forDeposit().admitPassport(passport);
                } else if (passport.getType() == PassportType.FOR_WITHDRAW) {
                    context.forWithdraw().admitPassport(passport);
                } else if (passport.getType() == PassportType.FOR_TRADE) {
                    context.forTrade().admitPassport(passport);
                }
            } catch (Exception ex) {
                LOG.error("RiskControl: execute admitPassport error", ex);
            }
        });
    }
}
