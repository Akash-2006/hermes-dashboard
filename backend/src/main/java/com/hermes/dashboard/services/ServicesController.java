package com.hermes.dashboard.services;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServicesController {

    private final HermesServiceService serviceService;

    public ServicesController(HermesServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceInfo> skills() {
        return serviceService.listSkills();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "hermesHomeFound", serviceService.hermesHomeExists(),
                "skillCount", serviceService.listSkills().size()
        );
    }
}
