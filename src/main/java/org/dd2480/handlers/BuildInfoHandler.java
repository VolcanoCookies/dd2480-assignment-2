package org.dd2480.handlers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.dd2480.builder.BuildResult;
import org.dd2480.builder.Builder;

import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 * Handles requests for retrieving and displaying detailed information
 * about a specific build.
 * 
 * This handler fetches build details using the commit hash, formats relevant
 * metadata, and renders the information using the `buildInfo.ftl` template.
 */
public class BuildInfoHandler implements Handler {

    private Builder builder;

    public BuildInfoHandler(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void handle(Context ctx) {
        String commitHash = ctx.pathParam("commitHash");
        BuildResult build = builder.getResult(commitHash);

        if (build == null) {
            ctx.status(404); // Build not found
        } else {
            var map = new HashMap<String, Object>();

            // Fills in variables present in /templates/buildInfo.ftl
            map.put("owner", build.repositoryOwner);
            map.put("repository", build.repositoryName);
            map.put("hash", build.commitHash);
            map.put("status", build.status.toString());
            switch (build.status) {
                case SUCCESS -> map.put("statusStyle", "status-success");
                case FAILURE -> map.put("statusStyle", "status-failure");
                case PENDING -> map.put("statusStyle", "status-pending");
                case ERROR -> map.put("statusStyle", "status-error");
            }
            map.put("branch", build.branch);
            map.put("logs", build.logs);
            
            // Format time to look presentable before putting it in the map
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZonedDateTime zonedDateTime = build.startTime.atZone(ZoneId.systemDefault());
            String formattedDateTime = zonedDateTime.format(formatter);
            map.put("date", formattedDateTime);

            // Render template with variables placed in map
            ctx.render("/templates/buildInfo.ftl", Map.of("build", map));
        }
    }
}