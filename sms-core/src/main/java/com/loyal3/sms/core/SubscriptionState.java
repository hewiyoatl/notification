package com.loyal3.sms.core;

public enum SubscriptionState {
    /**
     * User subscribed but yet to be verified
     */
    CREATED,

    /**
     * User requested to unsubscribed
     */
    UNSUBSCRIBED,

    /**
     * User subscribed and verified
     */
    SUBSCRIBED,

    /**
     * User is removed
     */
    DELETED,

    /**
     * Long code related was Decomissioned
     */
    TERMINATED
}
