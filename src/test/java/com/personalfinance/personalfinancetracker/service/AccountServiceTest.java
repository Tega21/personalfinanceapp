package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.AccountRequest;
import com.personalfinance.personalfinancetracker.dto.AccountResponse;
import com.personalfinance.personalfinancetracker.entity.Account;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.AccountRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private AccountRequest accountRequest;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        accountRequest = new AccountRequest();
        accountRequest.setName("Chase Checking");
        accountRequest.setType(Account.AccountType.CHECKING);
        accountRequest.setBalance(new BigDecimal("1500.00"));
        accountRequest.setInstitution("Chase");

        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setUser(testUser);
        testAccount.setName("Chase Checking");
        testAccount.setType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1500.00"));
        testAccount.setInstitution("Chase");
    }

    // Happy path: a valid request should create and return an account
    // response with the correct fields.
    @Test
    void createAccount_withValidData_returnsAccountResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.createAccount("testuser", accountRequest);

        assertNotNull(response);
        assertEquals("Chase Checking", response.getName());
        assertEquals(Account.AccountType.CHECKING, response.getType());
        assertEquals(new BigDecimal("1500.00"), response.getBalance());
        assertEquals("Chase", response.getInstitution());
    }

    // Creating an account for a username that doesn't exist should 404.
    @Test
    void createAccount_withNonExistentUser_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                accountService.createAccount("testuser", accountRequest));

        verify(accountRepository, never()).save(any(Account.class));
    }

    // Happy path: should return all accounts belonging to the user.
    @Test
    void getUserAccounts_returnsListOfAccounts() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUser_Id(1L)).thenReturn(List.of(testAccount));

        List<AccountResponse> result = accountService.getUserAccounts("testuser");

        assertEquals(1, result.size());
        assertEquals("Chase Checking", result.get(0).getName());
    }

    // A user with no accounts should get an empty list, not an error.
    @Test
    void getUserAccounts_withNoAccounts_returnsEmptyList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUser_Id(1L)).thenReturn(List.of());

        List<AccountResponse> result = accountService.getUserAccounts("testuser");

        assertTrue(result.isEmpty());
    }

    // Happy path: updating an account you own should succeed and
    // return the updated response.
    @Test
    void updateAccount_withValidOwnership_updatesSuccessfully() {
        AccountRequest updateRequest = new AccountRequest();
        updateRequest.setName("Chase Checking Updated");
        updateRequest.setType(Account.AccountType.CHECKING);
        updateRequest.setBalance(new BigDecimal("2000.00"));
        updateRequest.setInstitution("Chase");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.updateAccount("testuser", 1L, updateRequest);

        assertNotNull(response);
        verify(accountRepository).save(any(Account.class));
    }

    // Trying to update an account belonging to a different user should
    // be blocked and nothing saved.
    @Test
    void updateAccount_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("other").email("other@example.com").password("pw").build();
        Account otherAccount = new Account();
        otherAccount.setAccountId(1L);
        otherAccount.setUser(otherUser);
        otherAccount.setName("Other Account");
        otherAccount.setType(Account.AccountType.SAVINGS);
        otherAccount.setBalance(new BigDecimal("500.00"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(otherAccount));

        assertThrows(UnauthorizedActionException.class, () ->
                accountService.updateAccount("testuser", 1L, accountRequest));

        verify(accountRepository, never()).save(any(Account.class));
    }

    // Updating an account ID that doesn't exist should 404.
    @Test
    void updateAccount_withNonExistentAccount_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                accountService.updateAccount("testuser", 999L, accountRequest));

        verify(accountRepository, never()).save(any(Account.class));
    }

    // Happy path: deleting an account you own should succeed.
    @Test
    void deleteAccount_withValidOwnership_deletesSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        accountService.deleteAccount("testuser", 1L);

        verify(accountRepository).delete(testAccount);
    }

    // Trying to delete an account belonging to a different user should
    // be blocked and nothing deleted.
    @Test
    void deleteAccount_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("other").email("other@example.com").password("pw").build();
        Account otherAccount = new Account();
        otherAccount.setAccountId(1L);
        otherAccount.setUser(otherUser);
        otherAccount.setName("Other Account");
        otherAccount.setType(Account.AccountType.SAVINGS);
        otherAccount.setBalance(new BigDecimal("500.00"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(otherAccount));

        assertThrows(UnauthorizedActionException.class, () ->
                accountService.deleteAccount("testuser", 1L));

        verify(accountRepository, never()).delete(any(Account.class));
    }

    // Deleting an account ID that doesn't exist should 404.
    @Test
    void deleteAccount_withNonExistentAccount_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                accountService.deleteAccount("testuser", 999L));

        verify(accountRepository, never()).delete(any(Account.class));
    }

    // Net worth should return the summed balance from the repository.
    @Test
    void getNetWorth_returnsSummedBalance() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(accountRepository.sumBalanceByUserId(1L)).thenReturn(new BigDecimal("1500.00"));

        BigDecimal result = accountService.getNetWorth("testuser");

        assertEquals(new BigDecimal("1500.00"), result);
    }
}