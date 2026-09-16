package fr.simplex_software.workshop.conformance;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;

/**
 * Launches the MCP server (a runnable Spring Boot {@code -exec} jar) as a subprocess on a free port, for the
 * client conformance tests. Using a subprocess keeps the server fully isolated from each client's test JVM,
 * so the same helper works for the Spring Boot clients and the Quarkus one alike.
 *
 * <p>The jar to launch is taken from the {@code mcp.server.jar} system property, which each client module's
 * failsafe configuration points at the sibling server module's built artifact.
 */
public final class McpServerProcess implements AutoCloseable
{
  private final Process process;
  private final int port;

  private McpServerProcess(Process process, int port)
  {
    this.process = process;
    this.port = port;
  }

  public static McpServerProcess start(String... extraServerArgs)
  {
    var jar = System.getProperty("mcp.server.jar");
    if (jar == null || !new File(jar).isFile())
      throw new IllegalStateException("MCP server jar not found; set -Dmcp.server.jar to the server -exec jar (was: " + jar + ")");
    int port = freePort();
    try
    {
      var javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
      var command = new ArrayList<>(List.of(javaBin, "-jar", jar, "--server.port=" + port));
      command.addAll(List.of(extraServerArgs));
      var process = new ProcessBuilder(command)
        .redirectErrorStream(true)
        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
        .start();
      var server = new McpServerProcess(process, port);
      server.awaitReady(Duration.ofSeconds(90));
      return server;
    }
    catch (IOException e)
    {
      throw new IllegalStateException("Could not start MCP server from " + jar, e);
    }
  }

  /** Base URL of the running server; the MCP endpoint is {@code baseUrl() + "/mcp"}. */
  public String baseUrl()
  {
    return "http://localhost:" + port;
  }

  private void awaitReady(Duration timeout)
  {
    var client = HttpClient.newHttpClient();
    var request = HttpRequest.newBuilder(URI.create(baseUrl() + "/mcp"))
      .timeout(Duration.ofSeconds(2)).GET().build();
    var deadline = Instant.now().plus(timeout);
    while (Instant.now().isBefore(deadline))
    {
      if (!process.isAlive())
        throw new IllegalStateException("MCP server exited before becoming ready");
      try
      {
        client.send(request, HttpResponse.BodyHandlers.discarding()); // any HTTP status means it is up
        return;
      }
      catch (IOException e)
      {
        sleep(500); // not accepting connections yet
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while waiting for the MCP server", e);
      }
    }
    throw new IllegalStateException("MCP server did not become ready within " + timeout);
  }

  private static int freePort()
  {
    try (var socket = new ServerSocket(0))
    {
      return socket.getLocalPort();
    }
    catch (IOException e)
    {
      throw new IllegalStateException("Could not find a free port", e);
    }
  }

  private static void sleep(long millis)
  {
    try
    {
      Thread.sleep(millis);
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void close()
  {
    process.destroy();
    try
    {
      if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS))
        process.destroyForcibly();
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
    }
  }
}
