package com.fudn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountServiceLoginTest {

    private static final String USERNAME = "test01";
    private static final String EMAIL = "test@gmail.com";
    private static final String PASSWORD = "Strong@123";
    private static final String WRONG_PASSWORD = "Wrong@123";
    private static final String PHONE = "0912345678";
    private static final LocalDate DOB =
            LocalDate.of(2000, 1, 1);

    private AccountService service;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        service = new AccountService();

        assertEquals(
                ResultCode.SUCCESS,
                service.register(
                        USERNAME,
                        EMAIL,
                        PASSWORD,
                        PASSWORD,
                        DOB,
                        PHONE
                )
        );
    }

    // =========================================================
    // HELPER
    // =========================================================

    private Account account() {
        return service.findByUsername(USERNAME)
                .orElseThrow();
    }

    private void failLogin(int times) {

        for (int i = 0; i < times; i++) {
            service.login(USERNAME, WRONG_PASSWORD);
        }
    }

    // =========================================================
    // RULE 6
    // Correct login
    // =========================================================

    @Test
    void login_CorrectCredentials_ReturnsSuccess() {

        ResultCode result =
                service.login(USERNAME, PASSWORD);

        assertEquals(ResultCode.SUCCESS, result);
        assertEquals(0, account().getFailedAttempts());
        assertFalse(service.isLocked(USERNAME));
    }

    // =========================================================
    // LOG-01
    // Invalid input
    // =========================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void login_InvalidUsername_ReturnsInvalidInput(
            String username) {

        assertEquals(
                ResultCode.INVALID_INPUT,
                service.login(username, PASSWORD)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void login_InvalidPassword_ReturnsInvalidInput(
            String password) {

        assertEquals(
                ResultCode.INVALID_INPUT,
                service.login(USERNAME, password)
        );
    }

    // =========================================================
    // LOG-02
    // Username ignore case
    // =========================================================

    @Test
    void login_UsernameIgnoreCase_ReturnsSuccess() {

        assertEquals(
                ResultCode.SUCCESS,
                service.login("TEST01", PASSWORD)
        );
    }

    @Test
    void login_PasswordCaseSensitive_ReturnsInvalidCredentials() {

        assertEquals(
                ResultCode.INVALID_CREDENTIALS,
                service.login(USERNAME, "strong@123")
        );

        assertEquals(
                1,
                account().getFailedAttempts()
        );
    }

    // =========================================================
    // LOG-03
    // Unknown user
    // =========================================================

    @Test
    void login_UnknownUser_ReturnsInvalidCredentials() {

        assertEquals(
                ResultCode.INVALID_CREDENTIALS,
                service.login("unknown01", WRONG_PASSWORD)
        );
    }

    @Test
    void login_UnknownUserAndWrongPassword_ReturnSameCode() {

        ResultCode unknownUser =
                service.login("unknown01", PASSWORD);

        ResultCode wrongPassword =
                service.login(USERNAME, WRONG_PASSWORD);

        assertEquals(
                ResultCode.INVALID_CREDENTIALS,
                unknownUser
        );

        assertEquals(
                ResultCode.INVALID_CREDENTIALS,
                wrongPassword
        );
    }

    // =========================================================
    // LOG-04
    // Disabled account
    // =========================================================

    @ParameterizedTest
    @ValueSource(strings = {
            PASSWORD,
            WRONG_PASSWORD
    })
    void login_DisabledAccount_ReturnsAccountDisabled(
            String password) {

        assertEquals(
                ResultCode.SUCCESS,
                service.disableAccount(USERNAME)
        );

        assertEquals(
                ResultCode.ACCOUNT_DISABLED,
                service.login(USERNAME, password)
        );

        assertEquals(
                0,
                account().getFailedAttempts()
        );
    }

    // =========================================================
    // LOG-05
    // Wrong password 1-4 times
    // =========================================================

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void login_WrongPasswordLessThan5Times_IncrementsCounter(
            int times) {

        failLogin(times);

        assertEquals(
                ResultCode.INVALID_CREDENTIALS,
                service.login(USERNAME, WRONG_PASSWORD)
        );

        assertEquals(
                times + 1,
                account().getFailedAttempts()
        );

        assertFalse(
                service.isLocked(USERNAME)
        );
    }

    // =========================================================
    // LOG-05
    // Exactly 5 wrong attempts
    // =========================================================

    @Test
    void login_WrongPassword5thTime_LocksAccount() {

        failLogin(4);

        ResultCode result =
                service.login(USERNAME, WRONG_PASSWORD);

        assertEquals(
                ResultCode.ACCOUNT_LOCKED,
                result
        );

        assertEquals(
                5,
                account().getFailedAttempts()
        );

        assertTrue(
                service.isLocked(USERNAME)
        );
    }

    // =========================================================
    // LOG-06
    // Already locked
    // =========================================================

    @ParameterizedTest
    @ValueSource(strings = {
            PASSWORD,
            WRONG_PASSWORD
    })
    void login_WhileLocked_RejectsWithoutIncrement(
            String password) {

        failLogin(5);

        assertTrue(
                service.isLocked(USERNAME)
        );

        assertEquals(
                5,
                account().getFailedAttempts()
        );

        ResultCode result =
                service.login(USERNAME, password);

        assertEquals(
                ResultCode.ACCOUNT_LOCKED,
                result
        );

        // Không tăng thêm
        assertEquals(
                5,
                account().getFailedAttempts()
        );

        assertTrue(
                service.isLocked(USERNAME)
        );
    }

    // =========================================================
    // Boundary 3 / 4 / 5 / 6 failures
    // =========================================================

    @ParameterizedTest(name =
            "[{index}] {0} failures -> {1}, locked={2}")
    @CsvSource({
            "3, SUCCESS,        false",
            "4, SUCCESS,        false",
            "5, ACCOUNT_LOCKED, true",
            "6, ACCOUNT_LOCKED, true"
    })
    void login_CorrectPasswordAfterNFailures(
            int failures,
            ResultCode expected,
            boolean locked) {

        failLogin(failures);

        ResultCode result =
                service.login(USERNAME, PASSWORD);

        assertEquals(
                expected,
                result
        );

        assertEquals(
                locked,
                service.isLocked(USERNAME)
        );
    }

    // =========================================================
    // Successful login resets failed attempts
    // =========================================================

    @Test
    void login_SuccessAfterFailures_ResetsCounter() {

        failLogin(3);

        assertEquals(
                3,
                account().getFailedAttempts()
        );

        assertEquals(
                ResultCode.SUCCESS,
                service.login(USERNAME, PASSWORD)
        );

        assertEquals(
                0,
                account().getFailedAttempts()
        );

        assertFalse(
                service.isLocked(USERNAME)
        );
    }

    // =========================================================
    // Admin unlock
    // =========================================================

    @Test
    void login_AfterAdminUnlock_CounterRestartsAndCanLogin() {

        failLogin(5);

        assertTrue(
                service.isLocked(USERNAME)
        );

        assertEquals(
                5,
                account().getFailedAttempts()
        );

        assertEquals(
                ResultCode.SUCCESS,
                service.unlockAccount(USERNAME)
        );

        assertFalse(
                service.isLocked(USERNAME)
        );

        assertEquals(
                0,
                account().getFailedAttempts()
        );

        // Sai lại -> counter bắt đầu từ 1
        assertEquals(
                ResultCode.INVALID_CREDENTIALS,
                service.login(USERNAME, WRONG_PASSWORD)
        );

        assertEquals(
                1,
                account().getFailedAttempts()
        );

        // Đúng password -> SUCCESS
        assertEquals(
                ResultCode.SUCCESS,
                service.login(USERNAME, PASSWORD)
        );

        assertEquals(
                0,
                account().getFailedAttempts()
        );
    }

    // =========================================================
    // ADMIN: disableAccount
    // =========================================================

    @Test
    void disableAccount_ExistingUser_SetsDisabled() {

        assertEquals(
                ResultCode.SUCCESS,
                service.disableAccount(USERNAME)
        );

        assertEquals(
                AccountStatus.DISABLED,
                account().getStatus()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "nobody_1"
    })
    void disableAccount_BlankOrUnknown_ReturnsUserNotFound(
            String username) {

        assertEquals(
                ResultCode.USER_NOT_FOUND,
                service.disableAccount(username)
        );
    }

    // =========================================================
    // ADMIN: unlockAccount
    // =========================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "nobody_1"
    })
    void unlockAccount_BlankOrUnknown_ReturnsUserNotFound(
            String username) {

        assertEquals(
                ResultCode.USER_NOT_FOUND,
                service.unlockAccount(username)
        );
    }

    // =========================================================
    // FIND + IS LOCKED
    // =========================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "nobody_1"
    })
    void findByUsername_BlankOrUnknown_ReturnsEmpty(
            String username) {

        assertTrue(
                service.findByUsername(username).isEmpty()
        );

        assertFalse(
                service.isLocked(username)
        );
    }

    @Test
    void findByUsername_ExistingUser_ReturnsAccount() {

        assertTrue(
                service.findByUsername(USERNAME).isPresent()
        );

        assertEquals(
                USERNAME,
                service.findByUsername(USERNAME)
                        .orElseThrow()
                        .getUsername()
        );
    }

    @Test
    void isLocked_ExistingUnlockedUser_ReturnsFalse() {

        assertFalse(
                service.isLocked(USERNAME)
        );
    }
}