package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking,String successUrl,String failureUrl);
}
