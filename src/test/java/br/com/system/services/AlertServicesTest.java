package br.com.system.services;

import br.com.system.model.Alert;
import br.com.system.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServicesTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertServices alertServices;

    @Test
    void shouldMarkAlertAsReadAndRecordReadDate() {
        Alert alert = new Alert();
        alert.setId(1L);
        alert.setRead(false);
        alert.setActive(true);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(alert)).thenReturn(alert);

        LocalDateTime beforeMarkingAsRead = LocalDateTime.now();
        alertServices.markAsRead(1L);

        assertThat(alert.getRead()).isTrue();
        assertThat(alert.getReadAt()).isAfterOrEqualTo(beforeMarkingAsRead);
        assertThat(alert.getActive()).isTrue();
        verify(alertRepository).save(alert);
    }
}
