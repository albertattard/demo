package com.oracle.jsc.returns;

public enum CountryCode {
    DE("Germany", "49", "DEU"),
    DK("Denmark", "45", "DNK"),
    FR("France",  "33", "FRA"),
    GB("United Kingdom of Great Britain and Northern Ireland", "44", "GBR"),
    US("United States of America", "1", "USA");

    private final String usEnglishName;
    private final String dialCode;
    private final String alpha3;
    
    private CountryCode(String name, String dial, String a3) {
        usEnglishName = name;
        dialCode = dial;
        alpha3 = a3;
    }
    
    public String usEnglish() { return usEnglishName;}
    public String dial() { return dialCode;}
    public String alpha3() { return alpha3;}
}
