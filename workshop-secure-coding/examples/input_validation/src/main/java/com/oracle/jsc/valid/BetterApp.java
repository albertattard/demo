package com.oracle.jsc.valid;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Input validation demo
 */
public class BetterApp {
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        Map<String, Person> people = new HashMap<>();

        File in = new File("test.json");
        if (!in.exists()) {
            logError("File " + in.getAbsolutePath() + " does not appear to exist.");
            System.exit(10);
        }

        if (!in.canRead() || !in.isFile()) {
            logError("File " + in.getAbsolutePath() + " is either a directory or is not readable.");
            System.exit(10);
        }

        if (in.length() > 100 * 1024 * 1024) {
            logError("File " + in.getAbsolutePath() + " is larger than 100MB and can not be read.");
            System.exit(10);
        }

        Object rawData = null;

        try {
            rawData = parser.parse(new FileReader("test.json"));

        } catch (IOException | ParseException e) {
            logError("Exception thrown during parsing: " + e.getClass());
            logError("File " + in.getAbsolutePath() + " does not appear to be valid JSON; " + e.getMessage());
            System.exit(10);
        }

        if (rawData == null) {
            logError("File " + in.getAbsolutePath() + " produced no input.");
            System.exit(10);
        }

        if (!(rawData instanceof JSONArray)) {
            logError("File " + in.getAbsolutePath()
                    + " is not formatted as expected; the top-level element is not an array.");
            System.exit(10);
        }

        JSONArray data = (JSONArray) rawData;

        // build basic people
        for (int i = 0; i < data.size(); ++i) {
            Object o = (JSONObject) data.get(i);
            if (!(o instanceof JSONObject)) {
                logError("File " + in.getAbsolutePath() + " is not formatted as expected; top-level object # "
                        + (people.size() + 1) + " is not an object.");
                System.exit(10);
            }

            JSONObject jo = (JSONObject) o;

            String name = (String) jo.get("name");
            String city = (String) jo.get("city");
            String role = (String) jo.get("role");
            
            if (name == null) {
                logError("File " + in.getAbsolutePath() + " object # " + (people.size() + 1) + " does not have a name attribute.");
                System.exit(10);
            } else {
                city = (city == null ? "" : city);
                role = (role == null ? "" : role);

                people.put(name, new Person(name, city, role, null));
            }
        }

        // map staff
        for (int i = 0; i < data.size(); ++i) {
            JSONObject jo = (JSONObject) data.get(i);
            Object o = jo.get("staff");

            Person p = people.get(jo.get("name"));

            if (o == null) {
                people.put(p.name(), new Person(p.name(), p.city(), p.role(), new Person[0]));

            } else {
                if (!(o instanceof JSONArray)) {
                    logError("Staff for " + p.name() + " is mal-formed; expected an array of names.");
                    System.exit(10);
                }

                JSONArray staffArray = (JSONArray)o;

                List<Person> staff = new ArrayList<>();
                for (int j = 0; j < staffArray.size(); ++j) {
                    String stafferName = (String) staffArray.get(j);
                    if (stafferName == null) {
                        logError("found a null staffer name for " + p.name());  // not a fatal error
                    } else {
                        Person candidate = people.get(stafferName);
                        if (candidate == null) {
                            logError("\nStaffer \"" + stafferName + "\" for staff \"" + p.name() + "\" does not exist.\n");  // not a fatal error
                        } else {
                            staff.add(candidate);
                        }
                    }
                }

                people.put(p.name(), new Person(p.name(), p.city(), p.role(), staff.toArray(new Person[staff.size()])));
            }
        }

        emit(people);
    }

    private static void logError(String s) {
        System.err.println(s);
    }

    private static void emit(Map<String, Person> people) {
        for (Person p : people.values()) {
            System.out.println(p);
        }
    }
}
