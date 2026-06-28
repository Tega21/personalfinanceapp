package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.CreditCard;
import com.personalfinance.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * Repository for CreditCard entities. Defined in the schema ahead of
 * Sprint 3, when the controller/service and API endpoints
 * will be built.
 */
public interface CreditCardRepository extends JpaRepository<CreditCard,Long> {

    List<CreditCard> findByUser(User user);

    List<CreditCard> findByUserId(Long userId);

    
}
