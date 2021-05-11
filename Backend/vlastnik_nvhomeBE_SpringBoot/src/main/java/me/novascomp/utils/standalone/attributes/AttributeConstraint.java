package me.novascomp.utils.standalone.attributes;

import java.util.regex.Pattern;

public enum AttributeConstraint {
    //https://www.mkyong.com/regular-expressions/10-java-regular-expression-examples-you-should-know/
    //https://howtodoinjava.com/regex/java-regex-validate-international-phone-numbers/
    //https://zeroturnaround.com/rebellabs/java-regular-expressions-cheat-sheet/
    //https://www.oreilly.com/library/view/regular-expressions-cookbook/9781449327453/ch04s09.html

    PHONE_PATTERN("PHONE_CHECK", "^[+]?(?:[0-9] ?){8,11}[0-9]$", "NUMBERS ONLY"),
    DATE_PATTERN("DATE_CHECK", "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)", "DD/MM/YYYY"),
    ENAIL_PATTERN("EMAIL_CHECK", "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", "EXAMPLE@EXAMPLE.COM"),
    MAX_LENGTH_30("MAX_LENGTH_30", "^(?=.{1,30}$).*", "MAX LENGTH 30"),
    MAX_LENGTH_32("MAX_LENGTH_32", "^(?=.{1,32}$).*", "MAX LENGTH 32"),
    EXACT_LENGTH_36("MAX_LENGTH_36", "^(?=.{36,36}$).*", "EXACT LENGTH 36"),
    MAX_LENGTH_70("MAX_LENGTH_70", "^(?=.{1,70}$).*", "MAX LENGTH 70"),
    MAX_LENGTH_512("MAX_LENGTH_512", "^(?=.{1,512}$).*", "MAX LENGTH 512"),
    EXACT_LENGTH_8_DIGIT("MAX_LENGTH_8_DIGIT", "^([0-9]{8,8}$).*", "EXACT LENGTH 8 DIGIT"),
    MAX_LENGTH_250("MAX_LENGTH_250", "^(?=.{1,250}$).*", "MAX LENGTH 250"),
    //PASSWORD_PATTERN("MAX_LENGTH_30", "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,256})", "checkPattern", "AT LEAST ONCE: LOWERCASE, UPPERCASE; LENGTH: MIN 8, MAX 256"),
    //VERIFICATION
    TOKEN_KEY("TOKEN_KEY", "^[A-Za-z0-9_-]{23,23}$", "XXXXX-XXXXX-XXXXX-XXXXX"),
    NULL("NULL", "", "NULL");

    private final String name;
    private final String pattern;
    private final String hint;

    AttributeConstraint(String constraintName, String pattern, String hint) {
        this.name = constraintName;
        this.pattern = pattern;
        this.hint = hint;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public boolean checkPattern(String valueToValidate) {
        return Pattern.compile(pattern).matcher(valueToValidate).matches();
    }

    public String getHint() {
        return this.hint;
    }
}
