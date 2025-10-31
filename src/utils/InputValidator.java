package utils;

import java.util.Arrays;
import java.util.List;

public class InputValidator {

    private static final String[] SHS_COURSES = {
        "ABM", "STEM", "HUMSS", "TVL-ICT", "TVL-TO", "TVL-CA"
    };

    private static final String[] TERTIARY_COURSES = {
        "BSCS", "BSIT", "BSCpE", "BSBA", "BSA", "BSHM", "BMMA", "BSTM"
    };

    private static final String[] VALID_SIZES = {
        "XS", "S", "M", "L", "XL", "XXL", "One Size"
    };

    public static boolean isValidCourse(String course) {
        if (course == null) return false;
        String c = course.trim().toUpperCase();
        for (String s : SHS_COURSES) {
            if (s.equals(c)) return true;
        }
        for (String s : TERTIARY_COURSES) {
            if (s.equals(c)) return true;
        }
        return false;
    }

    public static boolean isValidSize(String size) {
        if (size == null) return false;
        String upperSize = size.toUpperCase();
        if (upperSize.equals("ONE SIZE") || upperSize.equals("ONESIZE")) {
            return true;
        }
        List<String> validSizes = Arrays.asList(VALID_SIZES);
        return validSizes.contains(upperSize);
    }

    public static String[] getAllValidCourses() {
        String[] all = new String[SHS_COURSES.length + TERTIARY_COURSES.length];
        System.arraycopy(SHS_COURSES, 0, all, 0, SHS_COURSES.length);
        System.arraycopy(TERTIARY_COURSES, 0, all, SHS_COURSES.length, TERTIARY_COURSES.length);
        return all;
    }

    public static String[] getSHSCourses() {
        return SHS_COURSES.clone();
    }

    public static String[] getTertiaryCourses() {
        return TERTIARY_COURSES.clone();
    }

    public static String[] getValidSizes() {
        return VALID_SIZES.clone();
    }
}