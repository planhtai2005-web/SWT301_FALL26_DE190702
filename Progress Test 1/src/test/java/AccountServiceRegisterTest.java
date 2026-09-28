package com.fudn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountServiceRegisterTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    // =========================================================
    // BR-REG-01: INVALID_INPUT
    // =========================================================

    @Test
    void register_NullUsername_ReturnsInvalidInput() {
        ResultCode result = service.register(
                null,
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_INPUT, result);
    }

    @Test
    void register_BlankUsername_ReturnsInvalidInput() {
        ResultCode result = service.register(
                "   ",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_INPUT, result);
    }

    @Test
    void register_NullEmail_ReturnsInvalidInput() {
        ResultCode result = service.register(
                "test01",
                null,
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_INPUT, result);
    }

    @Test
    void register_NullPassword_ReturnsInvalidInput() {
        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                null,
                null,
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_INPUT, result);
    }

    @Test
    void register_NullDateOfBirth_ReturnsInvalidInput() {
        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                null,
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_INPUT, result);
    }

    @Test
    void register_FutureDateOfBirth_ReturnsInvalidInput() {
        LocalDate future = LocalDate.now().plusDays(1);

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                future,
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_INPUT, result);
    }

    // =========================================================
    // BR-REG-02: INVALID_USERNAME
    // =========================================================

    @Test
    void register_InvalidUsername_ReturnsInvalidUsername() {
        ResultCode result = service.register(
                "abc",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_USERNAME, result);
    }

    // =========================================================
    // BR-REG-03: DUPLICATE_USERNAME
    // =========================================================

    @Test
    void register_DuplicateUsername_ReturnsDuplicateUsername() {

        ResultCode first = service.register(
                "test01",
                "test1@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        ResultCode second = service.register(
                "test01",
                "test2@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345679"
        );

        assertEquals(ResultCode.SUCCESS, first);
        assertEquals(ResultCode.DUPLICATE_USERNAME, second);
    }

    @Test
    void register_DuplicateUsernameCaseInsensitive_ReturnsDuplicateUsername() {

        service.register(
                "Test01",
                "test1@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        ResultCode result = service.register(
                "test01",
                "test2@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345679"
        );

        assertEquals(ResultCode.DUPLICATE_USERNAME, result);
    }

    // =========================================================
    // BR-REG-04: INVALID_EMAIL
    // =========================================================

    @Test
    void register_InvalidEmail_ReturnsInvalidEmail() {

        ResultCode result = service.register(
                "test01",
                "invalid-email",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_EMAIL, result);
    }

    // =========================================================
    // BR-REG-05: DUPLICATE_EMAIL
    // =========================================================

    @Test
    void register_DuplicateEmail_ReturnsDuplicateEmail() {

        ResultCode first = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        ResultCode second = service.register(
                "test02",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345679"
        );

        assertEquals(ResultCode.SUCCESS, first);
        assertEquals(ResultCode.DUPLICATE_EMAIL, second);
    }

    @Test
    void register_DuplicateEmailCaseInsensitive_ReturnsDuplicateEmail() {

        service.register(
                "test01",
                "Test@Gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        ResultCode result = service.register(
                "test02",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345679"
        );

        assertEquals(ResultCode.DUPLICATE_EMAIL, result);
    }

    // =========================================================
    // BR-REG-06: WEAK_PASSWORD
    // =========================================================

    @ParameterizedTest
    @MethodSource("invalidPasswords")
    void register_InvalidPassword_ReturnsWeakPassword(String password) {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                password,
                password,
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.WEAK_PASSWORD, result);
    }

    static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of("weak"),
                Arguments.of("weakpass"),
                Arguments.of("WEAKPASS"),
                Arguments.of("Weakpass"),
                Arguments.of("Weak1234"),
                Arguments.of("Weak@1234!")
        );
    }

    // =========================================================
    // BR-REG-07: PASSWORD_MISMATCH
    // =========================================================

    @Test
    void register_PasswordMismatch_ReturnsPasswordMismatch() {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@124",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.PASSWORD_MISMATCH, result);
    }

    // =========================================================
    // BR-REG-08: UNDERAGE
    // =========================================================

    @ParameterizedTest
    @MethodSource("underageDates")
    void register_Underage_ReturnsUnderage(LocalDate dob) {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                dob,
                "0912345678"
        );

        assertEquals(ResultCode.UNDERAGE, result);
    }

    static Stream<Arguments> underageDates() {

        LocalDate today = LocalDate.now();

        return Stream.of(
                Arguments.of(today.minusYears(17)),
                Arguments.of(today.minusYears(17).minusDays(1))
        );
    }

    // =========================================================
    // BR-REG-09: INVALID_PHONE
    // =========================================================

    @Test
    void register_NullPhone_ReturnsSuccess() {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                null
        );

        assertEquals(ResultCode.SUCCESS, result);
    }

    @Test
    void register_EmptyPhone_ReturnsSuccess() {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                ""
        );

        assertEquals(ResultCode.SUCCESS, result);
    }

    @Test
    void register_BlankPhone_ReturnsInvalidPhone() {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "   "
        );

        assertEquals(ResultCode.INVALID_PHONE, result);
    }

    @Test
    void register_InvalidPhone_ReturnsInvalidPhone() {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0123456789"
        );

        assertEquals(ResultCode.INVALID_PHONE, result);
    }

    // =========================================================
    // BR-REG-10: SUCCESS
    // =========================================================

    @Test
    void register_ValidInput_ReturnsSuccess() {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.SUCCESS, result);
    }

    @Test
    void register_ValidInput_CreatesAccount() {

        service.register(
                "test01",
                "test@gmail.com",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertTrue(service.findByUsername("test01").isPresent());
    }

    // =========================================================
    // PRIORITY ORDER
    // =========================================================

    @Test
    void register_InvalidUsernameAndEmail_ReturnsInvalidUsername() {

        ResultCode result = service.register(
                "abc",
                "invalid-email",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_USERNAME, result);
    }

    @Test
    void register_InvalidEmailAndPassword_ReturnsInvalidEmail() {

        ResultCode result = service.register(
                "test01",
                "invalid-email",
                "weak",
                "weak",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_EMAIL, result);
    }

    @Test
    void register_WeakPasswordAndMismatch_ReturnsWeakPassword() {

        ResultCode result = service.register(
                "test01",
                "test@gmail.com",
                "weak",
                "different",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.WEAK_PASSWORD, result);
    }

    // =========================================================
    // FAILED REGISTRATION MUST NOT CREATE ACCOUNT
    // =========================================================

    @Test
    void register_InvalidInput_DoesNotCreateAccount() {

        ResultCode result = service.register(
                "test01",
                "invalid-email",
                "Strong@123",
                "Strong@123",
                LocalDate.of(2000, 1, 1),
                "0912345678"
        );

        assertEquals(ResultCode.INVALID_EMAIL, result);
        assertFalse(service.findByUsername("test01").isPresent());
    }
}