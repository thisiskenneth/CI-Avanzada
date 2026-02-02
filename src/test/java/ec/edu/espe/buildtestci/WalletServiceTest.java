package ec.edu.espe.buildtestci;

import ec.edu.espe.buildtestci.dto.WalletResponse;
import ec.edu.espe.buildtestci.model.Wallet;
import ec.edu.espe.buildtestci.repository.WalletRepository;
import ec.edu.espe.buildtestci.service.RiskClient;
import ec.edu.espe.buildtestci.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    private WalletRepository walletRepository;
    private WalletService walletService;
    private RiskClient riskClient;

    @BeforeEach
    public void setup() {
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);
    }

    @Test
    void createWallet_validData_shouldSaveAndReturnResponse(){
        //Arrange
        String email = "kacortez@espe.edu.ec";
        double balance = 100.0;

        when(walletRepository.existsByOwnerEmail(email)).thenReturn(Boolean.FALSE);
        when(riskClient.isBlocked(email)).thenReturn(false);
        when(walletRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        //Act
        WalletResponse response = walletService.createWallet(email, balance);

        //Assert
        assertNotNull(response);
        assertEquals(100.0, response.getBalance());

        verify(riskClient).isBlocked(email);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).existsByOwnerEmail(email);
    }

    @Test
    void createWallet_invalidEmail_shouldThrow_andNotCallDependencies(){
        //Arrange
        String invalidEmail = "kacortez-espe.edu.ec";

        //Act+Assert
        assertThrows(IllegalArgumentException.class, () -> walletService.createWallet(invalidEmail, 50.0));

        //No debe llamar a ninguna dependencia porque falla la validacion
        verifyNoInteractions(walletRepository, riskClient);

    }

    @Test
    void deposit_walletNotFound_shouldThrow(){
        //Arrange
        String walletId = "no-exist-wallet";

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        //Act + Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> walletService.deposit(walletId, 60.0));

        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any());
    }


    @Test
    void deposit_shouldUpdateBalance_andSave_usingCaptor(){
        //Arrange
        Wallet wallet = new Wallet("kacortez@espe.edu.ec",300.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);

        //Act
        double newBalance = walletService.deposit(walletId, 300.0);

        //Assert
        assertEquals(600.0, newBalance);

        verify(walletRepository).save(captor.capture());
        Wallet saved = captor.getValue();
        assertEquals(600.0, saved.getBalance());

    }

    @Test
    void withdraw_insufficientFunds_shouldThrow_andNotSave(){
        //Arrange
        Wallet wallet = new Wallet("kacortez@espe.edu.ec",300);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        //Act + Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> walletService
                .withdraw(walletId,500));

        assertEquals("Insufficient funds", exception.getMessage());
        verify(walletRepository, never()).save(any());
    }

}
