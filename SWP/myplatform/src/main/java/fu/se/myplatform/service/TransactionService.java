package fu.se.myplatform.service;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Transaction;
import fu.se.myplatform.enums.TransactionMethod;
import fu.se.myplatform.enums.TransactionStatus;
import fu.se.myplatform.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// TransactionService.java
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void createAndSaveTransaction(Account account, long amount, String transactionCode, TransactionMethod method) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount); // Dùng amount truyền vào
        transaction.setTransactionCode(transactionCode);
        transaction.setMethod(method);
        transaction.setStatus(TransactionStatus.SUCCESS);

        transactionRepository.save(transaction);
    }
}