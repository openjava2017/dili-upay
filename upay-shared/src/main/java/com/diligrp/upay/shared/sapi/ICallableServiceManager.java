package com.diligrp.upay.shared.sapi;

import com.diligrp.upay.shared.domain.Message;
import com.diligrp.upay.shared.domain.MessageEnvelop;
import com.diligrp.upay.shared.domain.RequestContext;

public interface ICallableServiceManager {
    Message callService(RequestContext context, String payload) throws Throwable;

    Message callService(RequestContext context, MessageEnvelop envelop) throws Throwable;
}
