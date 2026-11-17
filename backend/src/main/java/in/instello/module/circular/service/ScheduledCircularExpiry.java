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
public class ScheduledCircularExpiry {

    private final CircularRepository circularRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireCirculars() {

        List<Circular> circulars =
            circularRepository.findByStatusAndExpiresAtLessThanEqual(
                CircularStatus.PUBLISHED,
                Instant.now()
            );

        for (Circular circular : circulars) {
            circular.setStatus(CircularStatus.EXPIRED);
        }

        circularRepository.saveAll(circulars);
    }
}