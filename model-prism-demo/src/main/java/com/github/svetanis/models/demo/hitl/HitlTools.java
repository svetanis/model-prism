package com.github.svetanis.models.demo.hitl;

import com.google.adk.events.EventActions;
import com.google.adk.tools.Annotations.Schema;

import java.util.Map;

/**
 * Custom toolset demonstrating how to trigger a HITL escalation.
 */
public class HitlTools {
  
  @Schema(
      name = "request_pr_approval", 
      description = "Requests manual Tech Lead approval before merging sensitive pull requests."
  )
  public EventActions requestPrApproval(
      @Schema(name = "pr_number", description = "The pull request number") int prNumber,
      @Schema(name = "reason", description = "Why manual approval is required") String reason) {
    
    System.out.println("\n🔒 SECURITY POLICY ALERT 🔒");
    System.out.println("Automated DevBot is requesting manual review for PR #" + prNumber);
    System.out.println("Reason: " + reason);
    System.out.println("Status: Escalating to Tech Lead...\n");

    return EventActions.builder()
        .escalate(true)
        .stateDelta(Map.of("escalation_type", "pr_approval", "pr_number", prNumber))
        .build();
  }

  @Schema(
      name = "merge_pr",
      description = "Merges a pull request after it has been approved."
  )
  public String mergePr(
      @Schema(name = "pr_number", description = "The pull request number") int prNumber) {
    System.out.println("\n✅ MERGING PR #" + prNumber + " ✅\n");
    return "Successfully merged PR #" + prNumber;
  }

  @Schema(
      name = "close_pr",
      description = "Closes a pull request if it has been rejected."
  )
  public String closePr(
      @Schema(name = "pr_number", description = "The pull request number") int prNumber,
      @Schema(name = "reason", description = "Reason for closing the PR") String reason) {
    System.out.println("\n❌ CLOSING PR #" + prNumber + " ❌");
    System.out.println("Reason: " + reason + "\n");
    return "Successfully closed PR #" + prNumber;
  }
}
