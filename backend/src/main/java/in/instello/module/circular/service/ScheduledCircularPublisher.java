package in.instello.module.circular.service;

import in.instello.module.circular.entity.Circular;
import in.instello.module.circular.entity.CircularStatus;
import in.instello.module.circular.repository.CircularRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScheduledCircularPublisher {

    private final CircularRepository circularRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void publishScheduledCirculars() {

        List<Circular> circulars =
            circularRepository.findByStatusAndScheduledAtLessThanEqual(
                CircularStatus.SCHEDULED,
                Instant.now()
            );

        for (Circular circular : circulars) {
            circular.setStatus(CircularStatus.PUBLISHED);
            circular.setPublishedAt(Instant.now());
            circular.setScheduledAt(null);
        }

        circularRepository.saveAll(circulars);
    }
}