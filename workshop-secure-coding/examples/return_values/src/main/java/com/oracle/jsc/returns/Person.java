package com.oracle.jsc.returns;

import java.util.Date;

public class Person {
    private final String name;
    private final Date birthDate;
    private PhoneNumber phone;
    private Person emergencyContact;

    public Person(String name, Date birthDate, PhoneNumber phone, Person emergencyContact) {
        this.name = name;
        this.birthDate = birthDate;
        this.phone = phone;
        this.emergencyContact = emergencyContact;
    }

    public String getName() {
        return name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public PhoneNumber getPhone() {
        return phone;
    }
    
    public void setPhone(PhoneNumber n) {
        phone = n;
    }

    public Person getEmergencyContact() {
        return emergencyContact;
    }
    public void setEmergencyContact(Person p) {
        emergencyContact = p;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Person{");
        result.append(name);
        result.append(", born ").append(birthDate);
        result.append(", phone: ").append(phone.dial());
        result.append(", contact: ").append(emergencyContact.getName());
        result.append("}");
        
        return result.toString();
    }
}
