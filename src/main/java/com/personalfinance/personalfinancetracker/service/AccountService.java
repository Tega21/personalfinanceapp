package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.AccountRequest;
import com.personalfinance.personalfinancetracker.dto.AccountResponse;
import com.personalfinance.personalfinancetracker.entity.Account;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.AccountRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for account management.
 * Handles all business logic for creating, retrieving, updating,
 * and deleting financial accounts. All operations are scoped to
 * the authenticated user to enforce data isolation.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new financial account for the authenticated user.
     *
     * @param username the username of the authenticated user
     * @param request  the account details from the request body
     * @return the created account as an AccountResponse DTO
     */
    @Transactional
    public AccountResponse createAccount(String username, AccountRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance());
        account.setInstitution(request.getInstitution());

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    /**
     * Retrieves all accounts belonging to the authenticated user.
     *
     * @param username the username of the authenticated user
     * @return list of AccountResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return accountRepository.findByUser_Id(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing account. Only the owner may update their account.
     *
     * @param username  the username of the authenticated user
     * @param accountId the ID of the account to update
     * @param request   the updated account details
     * @return the updated account as an AccountResponse DTO
     */
    @Transactional
    public AccountResponse updateAccount(String username, Long accountId, AccountRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to update this account");
        }

        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance());
        account.setInstitution(request.getInstitution());

        return mapToResponse(accountRepository.save(account));
    }

    /**
     * Deletes an account. Only the owner may delete their account.
     *
     * @param username  the username of the authenticated user
     * @param accountId the ID of the account to delete
     */
    @Transactional
    public void deleteAccount(String username, Long accountId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to delete this account");
        }

        accountRepository.delete(account);
    }

    /**
     * Calculates the total net worth for the authenticated user
     * by summing balances across all accounts.
     *
     * @param username the username of the authenticated user
     * @return the total net worth as a BigDecimal
     */
    @Transactional(readOnly = true)
    public BigDecimal getNetWorth(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return accountRepository.sumBalanceByUserId(user.getId());
    }

    /**
     * Maps an Account entity to an AccountResponse DTO.
     *
     * @param account the Account entity
     * @return the corresponding AccountResponse DTO
     */
    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getInstitution()
        );
    }
}