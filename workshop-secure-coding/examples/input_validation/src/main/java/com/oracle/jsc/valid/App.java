package com.oracle.jsc.valid;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Input validation demo
 */
public class App {
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        Map<String, Person> people = new HashMap<>();

        JSONArray data = (JSONArray) parser.parse(new FileReader("test.json"));

        // build basic people
        for (int i = 0; i < data.size(); ++i) {
            JSONObject o = (JSONObject) data.get(i);
            String name = (String) o.get("name");
            String city = (String) o.get("city");
            String role = (String) o.get("role");
            people.put(name, new Person(name, city, role, null));
        }
        // map staff
        for (int i = 0; i < data.size(); ++i) {
            JSONObject o = (JSONObject) data.get(i);
            JSONArray staffArray = (JSONArray) o.get("staff");

            if (staffArray != null) {
                Person p = people.get(o.get("name"));

                Person[] staff = new Person[staffArray.size()];
                for (int j = 0; j < staffArray.size(); ++j) {
                    String stafferName = (String) staffArray.get(j);
                    staff[j] = people.get(stafferName);
                }

                people.put(p.name(), new Person(p.name(), p.city(), p.role(), staff));
            }
        }

        emit(people);
    }

    private static void emit(Map<String, Person> people) {
        for (Person p : people.values()) {
            System.out.println(p);
        }
    }
}
