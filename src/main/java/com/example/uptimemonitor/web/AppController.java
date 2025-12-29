package com.example.uptimemonitor.web;

import com.example.uptimemonitor.domain.MonitoredApp;
import com.example.uptimemonitor.repo.MonitoredAppRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/apps")
public class AppController {

  private final MonitoredAppRepository apps;

  public AppController(MonitoredAppRepository apps) {
    this.apps = apps;
  }

  @GetMapping
  public List<AppDto> list() {
    return apps.findAll().stream().map(AppDto::from).toList();
  }

  @GetMapping("/{id}")
  public AppDto get(@PathVariable long id) {
    MonitoredApp app = apps.findById(id).orElseThrow(() -> new NoSuchElementException("App not found: " + id));
    return AppDto.from(app);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public AppDto create(@Valid @RequestBody UpsertAppRequest req) {
    MonitoredApp app = new MonitoredApp(req.name(), req.baseUrl(), req.healthPath(), req.enabled());
    return AppDto.from(apps.save(app));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public AppDto update(@PathVariable long id, @Valid @RequestBody UpsertAppRequest req) {
    MonitoredApp app = apps.findById(id).orElseThrow(() -> new NoSuchElementException("App not found: " + id));
    app.setName(req.name());
    app.setBaseUrl(req.baseUrl());
    app.setHealthPath(req.healthPath());
    app.setEnabled(req.enabled());
    return AppDto.from(apps.save(app));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable long id) {
    apps.deleteById(id);
  }

  public record UpsertAppRequest(
      @NotBlank String name,
      @NotBlank String baseUrl,
      String healthPath,
      boolean enabled
  ) {}

  public record AppDto(
      long id,
      String name,
      String baseUrl,
      String healthPath,
      boolean enabled
  ) {
    static AppDto from(MonitoredApp a) {
      return new AppDto(a.getId(), a.getName(), a.getBaseUrl(), a.getHealthPath(), a.isEnabled());
    }
  }
}
