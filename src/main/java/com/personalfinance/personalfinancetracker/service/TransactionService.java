package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.TransactionRequest;
import com.personalfinance.personalfinancetracker.dto.TransactionResponse;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.Transaction;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.repository.CategoryRepository;
import com.personalfinance.personalfinancetracker.repository.TransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionResponse createTransaction(TransactionRequest request, String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .category(category)
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);

    }

    public List<TransactionResponse> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteTransaction(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .categoryName(transaction.getCategory().getName())
                .categoryId(transaction.getCategory().getId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }


}
