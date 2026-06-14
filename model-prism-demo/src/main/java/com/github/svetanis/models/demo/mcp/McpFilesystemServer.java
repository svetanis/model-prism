package com.github.svetanis.models.demo.mcp;

import static java.lang.System.getProperty;

import io.modelcontextprotocol.client.transport.ServerParameters;
import com.google.adk.tools.mcp.StdioServerParameters;
import java.util.List;

/**
 * Common utility to configure the filesystem MCP server.
 */
public final class McpFilesystemServer {

  private McpFilesystemServer() {
  }

  /**
   * Builds the stdio server parameters for the
   * modelcontextprotocol/server-filesystem.
   *
   * @param root The root directory for the filesystem server to access.
   */
  public static ServerParameters params(String root) {
    String npx = getProperty("os.name").toLowerCase().contains("win") ? "npx.cmd" : "npx";
    return StdioServerParameters.builder()
        .command(npx)
        .args(List.of("-y", "@modelcontextprotocol/server-filesystem", root))
        .build()
        .toServerParameters();
  }
}
