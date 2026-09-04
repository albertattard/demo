package com.oracle.jsc.returns;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.oracle.jsc.returns.PhoneNumber.Exchange;
import com.oracle.jsc.returns.PhoneNumber.Phone;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        Person alice = new Person("Alice", new GregorianCalendar(1967, Calendar.JULY, 11).getTime(),
                new PhoneNumber(CountryCode.US, Exchange.FICTIONAL, Phone.get("7776021")), null);

        Person bob = new Person("Bob", new GregorianCalendar(1979, Calendar.NOVEMBER, 17).getTime(),
                new PhoneNumber(CountryCode.US, Exchange.FICTIONAL, Phone.get("7776021")), alice);

        alice.setEmergencyContact(bob);

        log("Alice: " + alice.toString());
        log("  Bob: " + bob.toString());
        log("-----");

        // say we innocently want to use Bob's birthday for some calculation
        Date bobsBirthday = bob.getBirthDate();
        // 90 days before Bob's birthday
        bobsBirthday.setTime(
                bobsBirthday.getTime() - (90 * 24 * 60 * 60 * 1000L));

        log("  Bob: " + bob.toString());
    }

    private static void log(String s) {
        System.out.println(s);
    }
}
