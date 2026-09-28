package com.fudn;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "alice",
            "Alice_01",
            "Z____"
    })
    void isValidUsername_Valid(String username) {
        assertEquals(true, AccountValidator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab_1",
            "1alice",
            "_alice",
            "ali ce",
            "alice!",
            "alice-01",
            ""
    })
    void isValidUsername_Invalid(String username) {
        assertEquals(false, AccountValidator.isValidUsername(username));
    }

    @ParameterizedTest(name = "[{index}] length={0} -> {1}")
    @MethodSource("usernameLengths")
    void isValidUsername_BoundaryLength(int length, boolean expected) {
        assertEquals(
                expected,
                AccountValidator.isValidUsername("a".repeat(length))
        );
    }

    static Stream<Arguments> usernameLengths() {
        return Stream.of(
                Arguments.of(4, false),
                Arguments.of(5, true),
                Arguments.of(6, true),
                Arguments.of(19, true),
                Arguments.of(20, true),
                Arguments.of(21, false)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "alice@example.com, true",
            "alice@test.com, true",
            "test.user@gmail.com, true",
            "alice@example, false",
            "alice@, false",
            "alice, false",
            "'', false"
    })
    void isValidEmail(String email, boolean expected) {
        assertEquals(expected, AccountValidator.isValidEmail(email));
    }

    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource(delimiter = '|', value = {
            "Secret@123    | alice_01 | true  | hợp lệ",
            "secret@123    | alice_01 | false | thiếu chữ hoa",
            "SECRET@123    | alice_01 | false | thiếu chữ thường",
            "Secret@abc    | alice_01 | false | thiếu chữ số",
            "Secret123     | alice_01 | false | thiếu ký tự đặc biệt",
            "'Secret @123' | alice_01 | false | chứa khoảng trắng",
            "Xalice_01@1   | alice_01 | false | chứa username",
            "Xalice_01@1   |          | true  | username null"
    })
    void isValidPassword_Partitions(
            String password,
            String username,
            boolean expected,
            String description) {

        assertEquals(
                expected,
                AccountValidator.isValidPassword(password, username)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "7, false",
            "8, true",
            "32, true",
            "33, false"
    })
    void isValidPassword_BoundaryLength(
            int length,
            boolean expected) {

        String password = "Aa1!" + "x".repeat(length - 4);

        assertEquals(
                expected,
                AccountValidator.isValidPassword(password, "alice")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0312345678",
            "0512345678",
            "0712345678",
            "0812345678",
            "0912345678"
    })
    void isValidPhone_Valid(String phone) {
        assertEquals(true, AccountValidator.isValidPhone(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0112345678",
            "0412345678",
            "0612345678",
            "0212345678",
            "091234567",
            "09123456789"
    })
    void isValidPhone_Invalid(String phone) {
        assertEquals(false, AccountValidator.isValidPhone(phone));
    }

    @ParameterizedTest(name = "[{index}] {0} -> {2} tuổi")
    @CsvSource({
            "2008-09-28, 2026-09-28, 18",
            "2008-09-29, 2026-09-28, 17",
            "2008-02-29, 2026-02-28, 17",
            "2008-02-29, 2026-03-01, 18"
    })
    void calculateAge_Boundaries(
            LocalDate dob,
            LocalDate today,
            int expected) {

        assertEquals(
                expected,
                AccountValidator.calculateAge(dob, today)
        );
    }
}