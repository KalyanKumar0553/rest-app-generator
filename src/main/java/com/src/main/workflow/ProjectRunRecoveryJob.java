package com.src.main.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.util.ProjectRunStatus;


@Component
public class ProjectRunRecoveryJob {

    private static final Logger log = LoggerFactory.getLogger(ProjectRunRecoveryJob.class);

    private final ProjectRunRepository projectRunRepository;
    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    // e.g. 30 minutes timeout
    private static final long STUCK_MINUTES = 30L;

    public ProjectRunRecoveryJob(ProjectRunRepository projectRunRepository) {
        this.projectRunRepository = projectRunRepository;
    }

    @Scheduled(fixedDelay = 300_000) // every 5 minutes
    @Transactional
    public void recoverStuckRuns() {
        OffsetDateTime threshold = OffsetDateTime.now(zoneId).minus(STUCK_MINUTES, ChronoUnit.MINUTES);
        
        List<ProjectRunEntity> stuck = projectRunRepository.findByStatusAndUpdatedAtBefore(ProjectRunStatus.INPROGRESS,threshold);
        if (stuck.isEmpty()) {
            return;
        }
        log.warn("Recovering {} stuck project runs older than {}", stuck.size(), threshold);
        for (ProjectRunEntity run : stuck) {
            run.setStatus(ProjectRunStatus.ERROR);
            if (run.getErrorMessage() == null) {
                run.setErrorMessage("Marked as ERROR by recovery job after inactivity");
            }
        }
    }
}
