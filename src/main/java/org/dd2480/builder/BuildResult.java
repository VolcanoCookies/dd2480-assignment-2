package org.dd2480.builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.dd2480.Commit;

/**
 * Encapsulates the result of a single build.
 */
public class BuildResult implements Serializable {

    public Commit commit;

    public BuildStatus status;
    public List<String> logs;

    public Instant startTime;
    public Instant endTime;

    public BuildResult(Commit commit, BuildStatus status, List<String> logs, Instant startTime, Instant endTime) {
        this.commit = commit;
        this.status = status;
        this.logs = logs;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public BuildResult() {

    }
}
