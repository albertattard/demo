package com.oracle.jsc.reflect;

import java.lang.reflect.Field;

/**
 * Hello world!
 */
public final class App {
    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
        Data data = new Data("Hello, world!");
        System.out.println("Data's secret is \"" + data.getSecret() + "\"");

        for (Field field : Data.class.getDeclaredFields()) {
            if (field.getName() == "secret") {
                field.setAccessible(true); // note that this affects the field object, not the instance!
                field.set(data, "Goodbye, cruel world!");
            }
        }

        System.out.println("Now data's secret is \"" + data.getSecret() + "\"");

        System.out.println("----");
        //
        // now for fix
        //
        FixedData fixedData = new FixedData("Hello, world!");
        System.out.println("FixedData's secret is \"" + fixedData.getSecret() + "\"");

        for (Field field : FixedData.class.getDeclaredFields()) {
            if (field.getName() == "secret") {
                field.setAccessible(true); // note that this affects the class, not the instance!
                // and now we can read it, but we can't write it
                try {
                    field.set(data, "Goodbye, cruel world!");
                } catch (IllegalArgumentException e) {
                    System.out.println("Caught " + e.getClass());
                }
            }
        }

        System.out.println("FixedData's secret is \"" + fixedData.getSecret() + "\"");

        System.out.println("----");
        //
        // And finally: a current option, for objects which don't need to be mutable in any way,
        //  and which can be run in Java versions 17 and later.
        //
        RecordData recordData = new RecordData("Hello, world!");
        
        System.out.println("RecordData's secret is \"" + recordData.secret() + "\"");

        for (Field field : RecordData.class.getDeclaredFields()) {
            if (field.getName() == "secret") {
                field.setAccessible(true); // note that this affects the class, not the instance!
                // and now we can read it, but we can't write it
                try {
                    field.set(recordData, "Goodbye, cruel world!");
                } catch (IllegalAccessException e) {
                    System.out.println("Caught " + e.getClass());
                }
            }
        }

        System.out.println("RecordData's secret is \"" + recordData.secret() + "\"");
    }
    
    private record RecordData(String secret) {}
}
