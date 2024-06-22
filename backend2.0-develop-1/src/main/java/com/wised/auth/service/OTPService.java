package com.wised.auth.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OTPService {

    private static final Integer EXPIRE_MINS = 4;
    private LoadingCache<String, Integer> otpCache;

    /**
     * Constructs an OTPService instance with a default expiration time for OTPs.
     * OTPs will expire after the specified number of minutes.
     */
    public OTPService() {
        super();

        // Create a cache for OTPs with an expiration policy
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    /**
     * Generates a new OTP (One-Time Password) for the given key.
     *
     * @param key The unique identifier associated with the OTP.
     * @return The generated OTP.
     */
    public int generateOTP(String key) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);

        // Store the generated OTP in the cache
        otpCache.put(key, otp);
        return otp;
    }

    /**
     * Retrieves the OTP (One-Time Password) associated with the given key.
     *
     * @param key The unique identifier associated with the OTP.
     * @return The OTP if found; otherwise, 0.
     */
    public int getOtp(String key) {
        try {
            return otpCache.get(key);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Clears the OTP (One-Time Password) associated with the given key.
     *
     * @param key The unique identifier associated with the OTP.
     */
    public void clearOTP(String key) {
        otpCache.invalidate(key);
    }
}
