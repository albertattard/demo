package com.oracle.jsc.returns;

import java.util.Objects;

public class PhoneNumber {
    public static final class Exchange {
        public static final Exchange FICTIONAL = new Exchange("555");

        private final String exchange;

        private Exchange(String ex) {
            exchange = ex;
        }

        public static Exchange get(String ex, CountryCode cc) {
            Objects.requireNonNull(ex);
            onlyDigits(ex, null);
            // verify things like not null, length, composed only of digits, and valid for the country code given.
            // if you really know what you're doing here, consider a flyweight.
            return new Exchange(ex);
        }
        
        public String dial() {
            return exchange;
        }
    }

    public static final class Phone {
        private final String phone;
        
        private Phone(String ph) {
            phone = ph;
        }
        
        public static Phone get(String ph) {
            Objects.requireNonNull(ph, "Phone may not be null.");
            onlyDigits(ph, null);
            // length, maybe?

            return new Phone(ph);
        }
        
        public String dial() {
            return phone;
        }
    }

    private final CountryCode countryCode;
    private final Exchange exchange;
    private final Phone phone;

    public PhoneNumber(CountryCode cc, Exchange ex, Phone ph) {
        // validate not null; everything else is already handled. Then...
        Objects.requireNonNull(cc, "Country code may not be null");
        Objects.requireNonNull(ex, "Exchange may not be null");
        Objects.requireNonNull(ph, "Phone number may not be null");

        countryCode = cc;
        exchange = ex;
        phone = ph;
    }

    public String dial() {
        return "+" + countryCode.dial() + "-" + exchange.dial() + "-" + phone.dial();
    }
    
    private static void onlyDigits(String s, String message) {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException(s + message == null ? " is required to be only digits." : message);
            }
        }
    }
}
